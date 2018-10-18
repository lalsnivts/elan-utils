package ru.msu.srcc.minlang;

import org.omg.CORBA.UserException;

/**
 * Created by User on 17.02.2015.
 */
public class ElanException extends UserException {
    public ElanException(String message) {
        super(message);
    }
}
