package com.unithub.controller;

import com.unithub.service.CheckinService;

public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }



}
