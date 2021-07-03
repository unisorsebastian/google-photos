package ro.jmind.photos.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ro.jmind.photos.service.CommandService;

@ShellComponent
public class Commands {

    private CommandService commandService;

    public Commands(CommandService commandService) {
        this.commandService = commandService;
    }

    @ShellMethod(
            value = "Exit the shell.",
            key = {"close", "shutdown"}
    )
    public void closeApplication() {
        System.exit(0);
    }

    @ShellMethod("Read data from excel 1")
    public String readExcelData(String excelFileName) {
        long startTime = System.currentTimeMillis();
        commandService.processFile(excelFileName);
        double timeTook = (double) Math.round((((double) (System.currentTimeMillis() - startTime) / 1000) * 100)) / 100;
        String format = String.format("done reading the excel file \"%s\" in %s seconds", excelFileName, timeTook);
        return format;
    }

}