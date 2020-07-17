package net.querybuilder4j.model.select_statement;

public enum Conjunction {

    And {
        @Override
        public String toString() {
            return "AND";
        }
    },
    Or {
        @Override
        public String toString() {
            return "OR";
        }
    },
    Empty {
        @Override
        public String toString() {
            return "";
        }
    }

}
