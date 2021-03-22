package net.querybuilder4j.sql.statement.criterion;

public enum Operator {

    equalTo {
        public String toString() {
            return "=";
        }
    },

    notEqualTo {
        public String toString() {
            return "<>";
        }
    },

    greaterThanOrEquals {
        public String toString() {
            return ">=";
        }
    },

    lessThanOrEquals {
        public String toString() {
            return "<=";
        }
    },

    greaterThan {
        public String toString() {
            return ">";
        }
    },

    lessThan {
        public String toString() {
            return "<";
        }
    },

    like {
        public String toString() {
            return "LIKE";
        }
    },

    notLike {
        public String toString() {
            return "NOT LIKE";
        }
    },

    in {
        public String toString() {
            return "IN";
        }
    },

    notIn {
        public String toString() {
            return "NOT IN";
        }
    },

    isNull {
        public String toString() {
            return "IS NULL";
        }
    },

    isNotNull {
        public String toString() {
            return "IS NOT NULL";
        }
    },

    between {
        public String toString() {
            return "BETWEEN";
        }
    },

    notBetween {
        public String toString() {
            return "NOT BETWEEN";
        }
    }

}
