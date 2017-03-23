package com.github.olloginov.support;

import lombok.Getter;
import lombok.ToString;

@ToString
public class DecisionMaker {
    @Getter
    private Decision decision = Decision.DEFAULT;

    public DecisionMaker() {
        this(Decision.DEFAULT);
    }

    public DecisionMaker(Decision decision) {
        this.decision = decision;
    }

    public void include() {
        decision = Decision.INCLUDE;
    }

    public void exclude() {
        decision = Decision.EXCLUDE;
    }
}
