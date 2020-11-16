# qb4j-api

## Local Development

To run the API locally and be able to debug it in IntelliJ, do the following:
1) Set up an IntelliJ Configuration like so (note the VM Option of `-DupdateCache=false`.  Change this to `true` to run 
the API as a one-time cache updater): ![IntelliJ Configuration](./readme-images/intellij_configuration.png)

2) Change the `qb4j.env` property to `local` in the `application.yml`.  This will cause the `Qb4j` object to be instantiated from
the `local-qb4j.yml` file.

3) To cache database metadata in the API's memory, change the `local-qb4j.yml` to look like this: ![In-memory local-qb4j.yml](./readme-images/local-qb4j-in-memory.png).  
To cache database metadata in a locally running Redis Docker container, change the `local-qb4j.yml` to look like this: ![Redis local-qb4j.yml](./readme-images/local-qb4j-redis.png).
If you cache database metadata in Redis, remember to start the Redis container using `docker-compose up -d`.  You'll also need
to run the IntelliJ Configuration that you set up in step #1 with a VM option of `-DupdateCache=true` so that Redis is populated with 
database metadata.  Do this before the next step or the Redis cache will be empty.


4) Start the IntelliJ Configuration that you set up in step #1.

To run the API locally, first build the project with:

1) `mvn clean install`
2) `java -Dqb4jConfig="$(cat qb4j.yml)" -jar "./target/qb4j-api-$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout).jar" `.
The command, `$(cat qb4j.yml)`, reads the `qb4j.yml` file contents into the `qb4jConfig` parameter which will be used for API configuration.
The command, `$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)`, reads the version from the `pom.xml` file.

To run the API with docker-compose, do the following:

1) Assign the qb4j.yml contents to the exported `QB4J_CONFIG` variable with `export QB4J_CONFIG=$(cat ./qb4j.yml)`.
2) Update the `docker-compose.yml` `api` service's image to the tag you want to run.
3) Update the `docker-compose.yml` `api` service's `update_cache` environment variable to `true` to run the image to refresh the 
database metadata cache or `false` to run the image as the API (TODO:  look into passing this is in with the `-e` option).

## Deployment

#### One Time Set Up

A CodeBuild pipeline must be created in order to build and deploy.  The CloudFormation template for the CodeBuild project
resource can be found at `cicd/cloudformation/one_time_setup/codebuild_pipeline/0.codebuild_pipeline.cloudformation.yaml`.  
You will need to use the AWS Console or AWS CLI to use this template to create the CodeBuild project resource. 

### DEV

#### One Time Set Up

A Lightsail instance must be created manually in order to host the `qb4j-mvc` docker image as a container that is exposed over
HTTP on the public internet.  You can create a Lightsail instance only through the AWS Console.  I used an Ubuntu 18.04 instance.
Although a Lightsail instance cannot be created through CloudFormation, it does allow you to insert a bootstrap shell script
when AWS is creating the instance.  Please use the script found at `cicd/cloudformation/one_time_setup/lightsail/lightsail-bootstrap.sh`
to configure the instance. 

#### CI/CD Pipeline

If the CodeBuild ENV environment variable is set to `dev`, then the CodeBuild pipeline will deploy to a Lightsail instance.  
This involves `ssh`ing into the Lightsail instance, pulling the new `qb4j-mvc` docker image, stopping the existing `qb4j-mvc`
docker container (there should only be 1 container running), and starting a new container based on the new `qb4j-mvc` image 
that was pulled. 

#### Common Gotchas

A common "gotcha" is if a docker image is running on the Lightsail instance and you delete that docker image tag from
Docker Hub, then run the CodeBuild pipeline to build a new image with the same tag.  These actions cause the old and new 
images to have the same tag, but different Docker Hub digest numbers (which act as an image ID).  When the CodeBuild pipeline
runs `DOCKER_CONTAINER_ID_TO_STOP=$(sudo docker ps | grep 'qb4j-mvc' | awk '{ print $1 }')` in the `cicd/deployment/deployment_dev.sh` 
file, the old image will no longer have a name like `qb4j-mvc`, because the image no longer exists in Docker Hub.  If you run 
`docker ps` during an `ssh` session on the Lightsail instance, you'll see that the running container will have a name that matches
the old image's digest number.  Therefore, the command will not be able to find a running container with a name like `qb4j-mvc`, 
so it will not be able to stop the container.  So, when `sudo nohup docker container run --publish 8080:8080 --detach joneschris/qb4j-mvc:"$DOCKER_IMAGE_TAG"`
is run, it will fail because there the old container is already using port `8080`.  To avoid all this, do the following if
you are rebuilding an image tag that has already been pushed to Docker Hub and is running in a container on the DEV Lightsail
instance:

1. Delete the image tag from Docker Hub
2. `ssh` into the DEV Lightsail instance and stop the running container
3. Now run the CodeBuild pipeline to build the new docker image, push it to Docker Hub, and deploy it to the DEV Lightsail 
instance.   

### PROD

#### One Time Set Up

There are no One Time Set Up steps at this time :)!

#### CI/CD Pipeline

If the CodeBuild ENV environment variable is set to `prod`, then the CodeBuild pipeline will create a CloudFormation stack(s) 
using the AWS CLI and the CloudFormation templates under the directory `cicd/cloudformation/stacks`.  Each CloudFormation 
template in the directory should be in the format of:

`<stack number>.<stack name>.cloudformation.yml`

The `cicd/buildspec_steps/1_post_build.bash` script will call the `cicd/deployment/deployment_prod.sh` script, which will 
iterate through each file in the `cicd/cloudformaation/stacks` directory that ends with `*.cloudformation.yaml` and use the
AWS CLI to create a stack with the name being the text in the `<stack name>` placeholder in the file name as shown above.

As of 7/5/2020, the only CloudFormation template in `cicd/cloudformation/stacks` is `1.qb4j-api-ecs-service.cloudformation.yaml`, 
which creates a new VPC, ECS service (which contains 1 task running the `qb4j-mvc` docker image), and a public internet-facing Application Load
Balancer which exposes the `qb4j-mvc` docker image to the internet.  Once this stack has been created, you can hit the API by
taking the Application Load Balancer's DNS Name (which can be found in the AWS Console) and prepending it with `http://` 
(ex:  `http://<ALB DNS Name>/metadata/database` would retrieve all databases from the API).

### Running the Image as a Scheduled Task for Database Metadata Cache Refreshing

You can use the following command to run the API's docker image as a scheduled task to refresh the database metadata cache
(you'll need to `cd` to the root of the directory to read `qb4j.yml` file correctly with `cat`): 

`docker run --env qb4j_config="$(cat ./qb4j.yml)" --entrypoint '/bin/sh' joneschris/qb4j-api:0.0.20 -c 'java -DupdateCache=true -Dqb4jConfig="$qb4j_config" -jar /qb4j/qb4j-api-0.0.20.jar'`

#### Common Gotchas

There are no Common Gotchas at this time :)!
