package org.idch.critspace;

import org.idch.critspace.persist.CritspaceRepository;
import org.idch.vprops.persist.PropertyRepository;

public class DBTest {

    private static void msg(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        try {
            CritspaceRepository repo = CritspaceRepository.get();
            PropertyRepository vpropRepo = PropertyRepository.get();
            msg("Checking database . . . .");
            if (repo.probe() && vpropRepo.probe()) {
                msg("the database is properly configured.");
            } else {
                msg("the database has not ben configured. Configuring now.");

                boolean success = false;
                if (!vpropRepo.probe()) {
                    msg("Creating visual properties database.");
                    success = vpropRepo.create();
                } else success = true;

                if (success && !repo.probe()) { 
                    msg("Creating visual properties database.");
                    success = repo.create();
                } 

                if (!repo.probe() || !vpropRepo.probe()) {
                    msg("Could not create database");
                    System.exit(2);
                } else {
                    msg("Created database");
                    System.exit(0);
                }
            }
            
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
