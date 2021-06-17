#!/bin/bash

# Copy the react app build files to the S3 bucket hosting the website.
aws s3 cp ./build s3://querybuilder4j.net --recursive
