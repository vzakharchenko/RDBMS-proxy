package com.github.vzakharchenko.backend.mysql.protocol;

public class MySQLAuthorization {
    private boolean step1;
    private boolean step2;
    private boolean step3;
    private boolean success;

    public boolean isStep1() {
        return step1;
    }

    public boolean isStep2() {
        return step2;
    }

    public boolean isStep3() {
        return step3;
    }

    public boolean isSuccess() {
        return success;
    }

    public void step1() {
        this.step1 = true;
        this.step2 = false;
        this.step3 = false;
        this.success = false;
    }

    public void step2() {
        this.step1 = true;
        this.step2 = true;
        this.step3 = false;
        this.success = false;
    }
    public void step3() {
        this.step1 = true;
        this.step2 = true;
        this.step3 = true;
        this.success = false;
    }

    public void success() {
        this.step1 = true;
        this.step2 = true;
        this.step3 = true;
        this.success = false;

    }
}
