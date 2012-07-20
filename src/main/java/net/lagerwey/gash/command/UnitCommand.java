package net.lagerwey.gash.command;

import net.lagerwey.gash.Gash;
import net.lagerwey.gash.Utils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;

import static net.lagerwey.gash.Utils.sortProcessingUnits;

/**
 * Prints a list of processing units.
 */
public class UnitCommand extends AbstractConnectedCommand {

    /**
     * Constructs this command with a Gash instance.
     *
     * @param gash Gash instance.
     */
    public UnitCommand(final Gash gash) {
        super(gash);
    }

    @Override
    public void perform(String command, String arguments) {
        Admin admin = gash.getWorkingLocation().getCurrentConnection().getAdmin();
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        ProcessingUnit[] processingUnitsArray = processingUnits.getProcessingUnits();
        sortProcessingUnits(processingUnitsArray);
        Utils.println("total %s units.", processingUnits.getSize());
        Utils.println("Status\t#Spaces\tName");
        Utils.println("--------------------------------------------------------------------------------");

        for (ProcessingUnit processingUnit : processingUnitsArray) {
            String spaces = (
                    processingUnit.getSpaces().length > 0 ? "" + processingUnit.getSpaces().length : "-");
            Utils.println("%s\t%s\t%s", processingUnit.getStatus(), spaces, processingUnit.getName());
        }
    }

    @Override
    public String description() {
        return "Lists all processing units";
    }
}
