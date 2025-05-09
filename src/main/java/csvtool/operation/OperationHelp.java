package csvtool.operation;

import csvtool.data.Const;
import csvtool.data.Context;
import csvtool.data.OptSettings;
import csvtool.enums.Operations;
import csvtool.enums.Settings;
import csvtool.utils.LogWrapper;

public class OperationHelp extends Operation
{
    private final LogWrapper LOGGER = new LogWrapper(this.getClass());

    public OperationHelp(Operations op)
    {
        super(op);
    }

    @Override
    public boolean runOperation(Context ctx)
    {
        if (ctx.getOpt().isQuiet())
        {
            super.toggleQuiet(true);
            LOGGER.toggleQuiet(true);
        }

        if (ctx.getOpt().isDebug())
        {
            super.toggleDebug(true);
            LOGGER.toggleDebug(true);
        }

        if (ctx.getOpt().isAnsiColors())
        {
            super.toggleAnsiColor(true);
            LOGGER.toggleAnsiColor(true);
        }

        LOGGER.debug("runOperation(): displayHelp()");

        if (ctx.hasInputFile())
        {
            // See if it's an Operation
            Operations type = Operations.fromArg(ctx.getInputFile());

            if (type != null)
            {
                Operation ops = type.init();

                if (ops != null)
                {
                    this.displayVersion();
                    ops.displayHelp();
                    return true;
                }
            }

            // See if it's a Setting
            Settings setting = Settings.fromArg(ctx.getInputFile());

            if (setting != null)
            {
                this.displayVersion();
                this.displayHelpForSetting(setting);
                return true;
            }
        }

        if (this.checkOptSettings(ctx.getOpt()))
        {
            return true;
        }

        // Generic Help
        this.displayVersion();
        this.displayHelp();
        System.out.print("\n");
        return true;
    }

    private void displayVersion()
    {
        System.out.printf("%s --\n", Const.STR);
        System.out.printf("\t(Author: %s)\n\n", Const.AUTHOR);
    }

    @Override
    public void displayHelp()
    {
        System.out.print("Help Operation:\n");
        System.out.printf("\tAliases: %s\n\n", Operations.HELP.getAlias().toString());

        // Default Help Screen
        System.out.printf("Usage: %s [--operation] <input> [settings]\n", Const.ID);
        System.out.print("\n");
        System.out.print("Operations (Must be presented first):\n");
        System.out.print("\t--help:\n\t\tThis Screen, optionally provide an Operation or Setting for more information.\n");
        System.out.print("\t--test:\n\t\tTest Routines [params: (input file)]\n");
        System.out.print("\t--merge:\n\t\tMerge Two CSV Files [requires: (input) (output) (key_field)]\n");
        System.out.print("\t--dedupe:\n\t\tDeDupe Two CSV Files  [requires: (input) (output) (key_field)]\n");
        System.out.print("\t--diff:\n\t\tDiff Two CSV Files  [requires: (input) (output) (key_field)]\n");
        System.out.print("\t--header-save:\n\t\tGenerate a headers.json from an input [requires: (input) (headers.json)]\n");
        System.out.print("\t--reformat:\n\t\tReformat A CSV File [requires: (input) (output) (headers.json)]\n");
        System.out.print("\t--transform-save:\n\t\tGenerate a transform.json from an input [requires: (input) (output) (transform.json)]\n");
        System.out.print("\t--transform-expand:\n\t\tExpand A CSV File's Columns using a transform.json using a key [requires: (input) (output) (transform.json) (key_field)]\n");
        System.out.print("\n");
        System.out.print("Settings (Cannot be listed before the operation, but it is accepted anywhere afterwards):\n");
        System.out.print("\t--input (file):\n\t\tSets the CSV Input File #2\n");
        System.out.print("\t--output (file):\n\t\tSets the Output File\n");
        System.out.print("\t--headers (file):\n\t\tSets the CSV Headers.json file.  This is used for \"Reformatting\" the CSV Fields like a config file.\n");
        System.out.print("\t--key (key):\n\t\tSets the CSV Key field.  This is used in MERGE, DIFF, and DEDUPE Operations so that they can compare the data.\n");
        System.out.print("\t--key2 (key):\n\t\tSets the CSV Key2 field for the DIFF/DEDUPE Operation.  This is used in DIFF and DEDUPE matching operations as a secondary data comparison.\n");
        System.out.print("\t--key3 (key):\n\t\tSets the CSV Key3 field for the DEDUPE Operation.  This is used in DEDUPE matching operations as a tertiary data comparison.\n");
        System.out.print("\t--side (key):\n\t\tSets the CSV Side field for the DIFF Operation.  This informs of which \"Side\" the DIFF output came from.\n");
        System.out.print("\t--de-dupe:\n\t\tSets the MERGE Operation in \"De-Duplication\" mode, which removes rows that already exists, and outputs this data to a separate file.\n");
        System.out.print("\t--squash-dupe:\n\t\tSets the MERGE/DEDUPE Operation in \"Squash\" mode, which combines rows that already exist with it's duplicates, and outputs the combined data.\n");
        System.out.print("\t--quotes:\n\t\tSets the CSV Output in \"Apply Quotes\" mode, which adds Quotes to all data, and not only when it is required.\n");
        System.out.print("\t--append:\n\t\tSets the CSV Output in \"Append\" mode, which causes the Output to not be Overwritten, but appended to.\n");
        System.out.print("\t--quiet:\n\t\tSets the Operation in \"Quiet\" mode, which causes the Logger messages to be suppressed.\n");
        System.out.print("\t--debug:\n\t\tSets the Operation in \"Debug\" mode, which causes the Logger to output Debug Level messages.\n");
        System.out.print("\t--ansi-colors:\n\t\tSets the Operation in \"Ansi-Color\" mode, which causes the Logger to output messages with ANSI-style color codes.\n");
        System.out.print("\n");
    }

    private void displayHelpForSetting(Settings setting)
    {
        switch (setting)
        {
            case INPUT2 -> this.displayHelpForInput2();
            case OUTPUT -> this.displayHelpForOutput();
            case HEADERS -> this.displayHelpForHeaders();
            case KEY -> this.displayHelpForKey();
            case KEY2 -> this.displayHelpForKey2();
            case KEY3 -> this.displayHelpForKey3();
            case SIDE -> this.displayHelpForSide();
            case DE_DUPE -> this.displayHelpForDeDupe();
            case SQUASH_DUPE -> this.displayHelpForSquashDupe();
            case QUOTES -> this.displayHelpForQuotes();
            case APPEND -> this.displayHelpForAppend();
            case QUIET -> this.displayHelpForQuiet();
            case DEBUG -> this.displayHelpForDebug();
            case ANSI_COLORS -> this.displayHelpForAnsiColors();
            default -> this.displayHelp();
        }

        System.out.print("\n");
    }

    private boolean checkOptSettings(OptSettings opt)
    {
        boolean hasOpt = false;

        if (opt.hasInput2())
        {
            this.displayVersion();
            this.displayHelpForInput2();
            hasOpt = true;
        }
        else if (opt.hasOutput())
        {
            this.displayVersion();
            this.displayHelpForOutput();
            hasOpt = true;
        }
        else if (opt.hasHeaders())
        {
            this.displayVersion();
            this.displayHelpForHeaders();
            hasOpt = true;
        }
        else if (opt.hasKey())
        {
            this.displayVersion();
            this.displayHelpForKey();
            hasOpt = true;
        }
        else if (opt.hasKey2())
        {
            this.displayVersion();
            this.displayHelpForKey2();
            hasOpt = true;
        }
        else if (opt.hasKey3())
        {
            this.displayVersion();
            this.displayHelpForKey3();
            hasOpt = true;
        }
        else if (opt.hasSide())
        {
            this.displayVersion();
            this.displayHelpForSide();
            hasOpt = true;
        }
        else if (opt.isDeDupe())
        {
            this.displayVersion();
            this.displayHelpForDeDupe();
            hasOpt = true;
        }
        else if (opt.isSquashDupe())
        {
            this.displayVersion();
            this.displayHelpForSquashDupe();
            hasOpt = true;
        }
        else if (opt.isApplyQuotes())
        {
            this.displayVersion();
            this.displayHelpForQuotes();
            hasOpt = true;
        }
        else if (opt.isAppendOutput())
        {
            this.displayVersion();
            this.displayHelpForAppend();
            hasOpt = true;
        }
        else if (opt.isDebug() && !Const.DEBUG)
        {
            this.displayVersion();
            this.displayHelpForDebug();
            hasOpt = true;
        }
        else if (opt.isQuiet() && !Const.QUIET)
        {
            this.displayVersion();
            this.displayHelpForQuiet();
            hasOpt = true;
        }
        else if (opt.isAnsiColors() && !Const.ANSI_COLOR)
        {
            this.displayVersion();
            this.displayHelpForAnsiColors();
            hasOpt = true;
        }

        System.out.print("\n");

        return hasOpt;
    }

    private void displayHelpForInput2()
    {
        System.out.print("--input (file):\n");
        System.out.printf("Aliases: %s\n", Settings.INPUT2.getAlias().toString());
    }

    private void displayHelpForOutput()
    {
        System.out.print("--output (file):\n");
        System.out.printf("Aliases: %s\n", Settings.OUTPUT.getAlias().toString());
    }

    private void displayHelpForHeaders()
    {
        System.out.print("--headers (file):\n");
        System.out.printf("Aliases: %s\n", Settings.HEADERS.getAlias().toString());
    }

    private void displayHelpForKey()
    {
        System.out.print("--key (field):\n");
        System.out.printf("Aliases: %s\n", Settings.KEY.getAlias().toString());
    }

    private void displayHelpForKey2()
    {
        System.out.print("--key2 (field):\n");
        System.out.printf("Aliases: %s\n", Settings.KEY2.getAlias().toString());
    }

    private void displayHelpForKey3()
    {
        System.out.print("--key3 (field):\n");
        System.out.printf("Aliases: %s\n", Settings.KEY3.getAlias().toString());
    }

    private void displayHelpForSide()
    {
        System.out.print("--side (field):\n");
        System.out.printf("Aliases: %s\n", Settings.SIDE.getAlias().toString());
    }

    private void displayHelpForDeDupe()
    {
        System.out.print("--squash-dupe:\n");
        System.out.printf("Aliases: %s\n", Settings.SQUASH_DUPE.getAlias().toString());
    }

    private void displayHelpForSquashDupe()
    {
        System.out.print("--de-dupe:\n");
        System.out.printf("Aliases: %s\n", Settings.DE_DUPE.getAlias().toString());
    }

    private void displayHelpForQuotes()
    {
        System.out.print("--quotes:\n");
        System.out.printf("Aliases: %s\n", Settings.QUOTES.getAlias().toString());
    }

    private void displayHelpForAppend()
    {
        System.out.print("--append:\n");
        System.out.printf("Aliases: %s\n", Settings.APPEND.getAlias().toString());
    }

    private void displayHelpForQuiet()
    {
        System.out.print("--quiet:\n");
        System.out.printf("Aliases: %s\n", Settings.QUIET.getAlias().toString());
    }

    private void displayHelpForDebug()
    {
        System.out.print("--debug:\n");
        System.out.printf("Aliases: %s\n", Settings.DEBUG.getAlias().toString());
    }

    private void displayHelpForAnsiColors()
    {
        System.out.print("--ansi-colors:\n");
        System.out.printf("Aliases: %s\n", Settings.ANSI_COLORS.getAlias().toString());
    }

    @Override
    public void clear()
    {
        // NO-OP
    }
}
