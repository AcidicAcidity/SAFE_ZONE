package com.safeZone.views;

import com.googlecode.lanterna.*;
import org.slf4j.*;

public class AuthMenu {
    private static final Logger log = LoggerFactory.getLogger(AuthMenu.class);
    private DBHelper dbHelper;

    public AuthMenu(DBHelper dbHelper){
        this.dbHelper = dbHelper;
    }
    public void start() throws Exception {

    }
}
