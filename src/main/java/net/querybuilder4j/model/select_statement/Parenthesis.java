package net.querybuilder4j.model.select_statement;

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
