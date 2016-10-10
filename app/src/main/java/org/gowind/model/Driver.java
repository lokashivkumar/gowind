package org.gowind.model;

import java.util.UUID;

/**
 * Created by shiv.loka on 10/3/16.
 */

public class Driver extends User {
    private boolean backgroundVerified;
    private long lastUpdatedBkgdVerificationDate;
    private long dateOfBirth;
    private UUID referredBy;
    private long joinDate;
}
