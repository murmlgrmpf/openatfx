package de.rechner.openatfx.main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.asam.ods.AoException;
import org.asam.ods.AoFactory;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import de.rechner.openatfx.AoServiceFactory;


public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class);

    public static void main22(String[] args) {
        try {
            BasicConfigurator.configure();

            // configure ORB
            ORB orb = ORB.init(new String[0], System.getProperties());
            AoFactory aoFactory = AoServiceFactory.getInstance().newAoFactory(orb);

            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            NameComponent path[] = ncRef.to_name("ATFX");
            ncRef.rebind(path, aoFactory);

            LOG.info("ATFX Server started");
            orb.run();
        } catch (InvalidName e) {
            System.err.println(e.getMessage());
        } catch (NotFound e) {
            System.err.println(e.getMessage());
        } catch (CannotProceed e) {
            System.err.println(e.getMessage());
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
            System.err.println(e.getMessage());
        } catch (AoException e) {
            System.err.println(e.reason);
        }
    }

    public static void main(String[] args) {
        String input = "/[prj]no_project/[tstser]Test_Vorbeifahrt/[mea]Run_middEng_FINAL_RES/[dts]Detector\\;rms A fast - Zusammenfassung";
        // (?<!\\)/\[(.*(?<!\\))\](.*(?<!\\));
        Pattern pattern = Pattern.compile("(?<!\\\\)/\\[(.*(?<!\\\\))\\](.*(?<!\\\\));");
        Matcher m = pattern.matcher(input);
        
        System.out.println(m.matches());
        
        while (m.find()) {
            System.out.println("---------------------------------");
            System.out.println(m.group(0));
            System.out.println("AE: " + m.group(1));
            System.out.println("IE: " + m.group(2));
            // System.out.println("VE: " + m.group(3));
        }
    }

}
