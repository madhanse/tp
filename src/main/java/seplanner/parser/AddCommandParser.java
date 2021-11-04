package seplanner.parser;

import seplanner.commands.AddMapCommand;
import seplanner.commands.AddModCommand;
import seplanner.commands.AddUniCommand;
import seplanner.commands.Command;
import seplanner.constants.Constants;
import seplanner.exceptions.AddParseException;
import seplanner.modules.Module;
import seplanner.modules.ModuleList;
import seplanner.universities.University;
import seplanner.universities.UniversityList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddCommandParser {

    private String flag;
    private int uniIndex;
    private int mapIndex;
    private University university;
    private Module module;
    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    public Command parse(String arguments, UniversityList universityMasterList,
                         ModuleList moduleMasterList, UniversityList universitySelectedList,
                         ModuleList moduleSelectedList) throws AddParseException, IOException {

        logger.log(Level.INFO, Constants.LOGMSG_PARSESTARTED);

        String flagArguments = identifyFlagAndSplitArgs(arguments);

        switch (flag) {
        case Constants.FLAG_UNIVERSITY:
            handleUniFlagArgs(flagArguments, universityMasterList, universitySelectedList);
            logger.log(Level.INFO, Constants.LOGMSG_PARSESUCCESS);
            return new AddUniCommand(university, universityMasterList, universitySelectedList);
        case Constants.FLAG_MODULE:
            handleModFlagArgs(flagArguments, moduleMasterList, moduleSelectedList);
            logger.log(Level.INFO, Constants.LOGMSG_PARSESUCCESS);
            return new AddModCommand(module, moduleMasterList, moduleSelectedList);
        case Constants.FLAG_MAP:
            handleMapFlagArgs(flagArguments, universitySelectedList, moduleSelectedList, universityMasterList);
            logger.log(Level.INFO, Constants.LOGMSG_PARSESUCCESS);
            return new AddMapCommand(uniIndex, mapIndex,universityMasterList, moduleMasterList,
                    universitySelectedList, moduleSelectedList);
        default:
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_INCORRECTFLAGS, 1);
        }
    }

    private String identifyFlagAndSplitArgs(String arguments) throws AddParseException {
        String[] argumentsSubstrings = arguments.trim().split(" ", 2);
        if (ParseCondition.isMissingArguments(argumentsSubstrings)) {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_MISSINGARGUMENTS, 1);
        }
        flag = argumentsSubstrings[0].trim();
        return argumentsSubstrings[1].trim();
    }

    private void handleUniFlagArgs(String arguments, UniversityList universityMasterList,
                                   UniversityList universitySelectedList) throws AddParseException {
        String uniName;
        if (ParseCondition.isText(arguments)) {
            uniName = arguments;
            // Check if university exists
            if (!ParseCondition.isValidUniversity(universityMasterList, uniName)) {
                logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
                throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_UNINOTFOUND, 1);
            }
            university = new University(uniName, new ArrayList<>(), universityMasterList);
        } else if (ParseCondition.isNumeric(arguments)) {
            uniIndex = Integer.parseInt(arguments);
            // Check if university exists
            if (ParseCondition.isIndexOutOfBounds(uniIndex, universityMasterList)) {
                logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
                throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_UNINOTFOUND, 1);
            }
            uniName = universityMasterList.get(uniIndex - 1).getName();
            university = new University(uniName, new ArrayList<>(), uniIndex);
        } else {
            // in the case where input is both not text only and numbers only
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_UNINOTFOUND, 1);
        }

        // Check if university has been added already
        if (ParseCondition.isDuplicateUniversity(universitySelectedList, uniName)) {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_DUPLICATEUNI, 1);
        }
    }

    private void handleModFlagArgs(String arguments, ModuleList moduleMasterList,
                                   ModuleList moduleSelectedList) throws AddParseException {
        if (ParseCondition.isText(arguments)) {
            module = moduleMasterList.getModule(arguments.toUpperCase());
            // Check if module exists
            if (ParseCondition.isNullModule(module)) {
                logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
                throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_MODNOTFOUND, 1);
            }
        } else if (ParseCondition.isNumeric(arguments)) {
            int modIndex = Integer.parseInt(arguments);
            // Check if module exists
            if (ParseCondition.isIndexOutOfBounds(modIndex, moduleMasterList)) {
                logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
                throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_MODNOTFOUND, 1);
            }
            module = moduleMasterList.get(modIndex - 1);
        } else {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_MODNOTFOUND, 1);
        }

        // Check if module has been added already
        if (ParseCondition.isDuplicateModule(moduleSelectedList, module)) {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_DUPLICATEMOD, 1);
        }
    }

    private void handleMapFlagArgs(String arguments, UniversityList universitySelectedList,
                                   ModuleList moduleSelectedList,
                                   UniversityList universityMasterList) throws AddParseException {
        // Separate arguments
        String[] argumentSubstrings = arguments.trim().split(" ", 2);
        if (ParseCondition.isMissingArguments(argumentSubstrings)) {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_MISSINGARGUMENTS, 1);
        }
        String firstParam = argumentSubstrings[0].trim();
        String secondParam = argumentSubstrings[1].trim();
        if (ParseCondition.isNumeric(firstParam) && ParseCondition.isNumeric(secondParam)) {
            uniIndex = Integer.parseInt(firstParam);
            mapIndex = Integer.parseInt(secondParam);
        } else {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            String error = (ParseCondition.isNumeric(firstParam)) ? Constants.ERRORMSG_PARSEEXCEPTION_INVALIDMAPPING
                    : Constants.ERRORMSG_PARSEEXCEPTION_INVALIDUNI;
            throw new AddParseException(error, 1);
        }
        University currentUni = ParseCondition.getSelectedUniObject(uniIndex, universitySelectedList,
                universityMasterList);
        if (!ParseCondition.isInSelectedUniList(uniIndex, universitySelectedList, universityMasterList)) {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_INVALIDUNI, 1);
        }
        if (ParseCondition.isMissingAvailableMapping(uniIndex, universityMasterList, moduleSelectedList)) {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_NOMAPPING, 1);
        }
        if (ParseCondition.isIndexOutOfBounds(uniIndex, mapIndex, universityMasterList, moduleSelectedList)) {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_INVALIDMAPPING, 1);
        }
        if (ParseCondition.isDuplicateMapping(currentUni, uniIndex, mapIndex, universityMasterList, moduleSelectedList)) {
            logger.log(Level.WARNING, Constants.LOGMSG_PARSEFAILED);
            throw new AddParseException(Constants.ERRORMSG_PARSEEXCEPTION_DUPLICATEMAP, 1);
        }
    }

}