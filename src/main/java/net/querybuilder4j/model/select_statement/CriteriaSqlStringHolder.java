package net.querybuilder4j.model.select_statement;

import java.util.ArrayList;
import java.util.List;

public class CriteriaSqlStringHolder {

    private List<String> criterionSqlStrings = new ArrayList<>();

    private int numOfOpeningParenthesisInBranch = 0;

    private int numOfClosingParenthesisInBranch = 0;

    public List<String> getCriterionSqlStrings() {
        return criterionSqlStrings;
    }

    public void addSqlString(Criterion criterion, char beginningDelimiter, char endingDelimiter) {
        String criterionSqlString = criterion.toSql(beginningDelimiter, endingDelimiter);
        criterionSqlStrings.add(criterionSqlString);

        if (criterion.hasOpeningParenthesis()) {
            numOfOpeningParenthesisInBranch++;
        }

        if (criterion.hasClosingParenthesis()) {
            numOfClosingParenthesisInBranch += criterion.getClosingParenthesis().size();
        }
    }

    public int getDiffOfOpeningAndClosingParenthesis() {
        return this.numOfOpeningParenthesisInBranch - numOfClosingParenthesisInBranch;
    }

    public void resetNumOfOpeningAndClosingParenthesis() {
        this.numOfOpeningParenthesisInBranch = 0;
        this.numOfClosingParenthesisInBranch = 0;
    }

    public String getSqlStringRepresentation() {
        return String.join(" ", this.getCriterionSqlStrings());
    }

}
