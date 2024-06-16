package com.offer.java.offer.mapper;

import com.offer.java.offer.dto.ScriptInfo;
import com.offer.java.offer.dto.ScriptInfoResponse;
import com.offer.java.offer.dto.ScriptInfoShort;
import org.modelmapper.ModelMapper;


public class MapperScript {

    private static final ModelMapper mapper = new ModelMapper();

    public static ScriptInfoShort mapToShortInfo(ScriptInfo script) {
        return mapper.map(script, ScriptInfoShort.class);
    }

    public static ScriptInfoResponse mapToResponseScript(ScriptInfo script) {
        return mapper.map(script, ScriptInfoResponse.class);
    }

}
