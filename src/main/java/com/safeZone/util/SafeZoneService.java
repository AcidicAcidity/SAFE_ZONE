package com.safeZone.util;

import com.safeZone.model.Bin;
import com.safeZone.model.Payment;
import com.safeZone.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SafeZoneService {

    private static final Logger log = LoggerFactory.getLogger(SafeZoneService.class);

    private final DBHelper db;

    public SafeZoneService(DBHelper db) {
        this.db = db;
    }