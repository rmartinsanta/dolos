package org.example;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.nio.file.Paths;

public class PathValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        var f = Paths.get(value).toFile();
        if(!f.exists() || !f.isDirectory()){
            throw new ParameterException("Parameter %s must be an existing folder, path %s does not exist or it is not a folder".formatted(name, value));
        }
    }
}
