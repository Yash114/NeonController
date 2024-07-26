package com.gingertech.starbeam.limelight.computers;

import com.gingertech.starbeam.limelight.nvstream.http.ComputerDetails;

public interface ComputerManagerListener {
    void notifyComputerUpdated(ComputerDetails details);
}
