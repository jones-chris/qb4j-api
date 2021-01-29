package net.querybuilder4j.sql.statement.criterion;

public enum Parenthesis {

    FrontParenthesis {
        public String toString() {
            return "(";
        }
    },

    EndParenthesis {
        public String toString() {
            return ")";
        }
    },

    Empty {
        @Override
        public String toString() {
            return "";
        }
    }

}
