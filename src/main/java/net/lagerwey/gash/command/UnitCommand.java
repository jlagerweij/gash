package net.lagerwey.gash.command;

import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;

import static net.lagerwey.gash.Utils.sortProcessingUnits;

/**
 * Prints a list of processing units.
 */
public class UnitCommand implements Command {

    @Override
    public void perform(Admin admin, String command, String arguments) {
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        ProcessingUnit[] processingUnitsArray = processingUnits.getProcessingUnits();
        sortProcessingUnits(processingUnitsArray);
        Utils.info("total %s units.", processingUnits.getSize());
        Utils.info("Status\t#Spaces\tName");
        Utils.info("--------------------------------------------------------------------------------");

        for (ProcessingUnit processingUnit : processingUnitsArray) {
            String spaces = (
                    processingUnit.getSpaces().length > 0 ? "" + processingUnit.getSpaces().length : "-");
            Utils.info("%s\t%s\t%s", processingUnit.getStatus(), spaces, processingUnit.getName());
        }
    }

    @Override
    public String description() {
        return "Lists all processing units.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }

}
