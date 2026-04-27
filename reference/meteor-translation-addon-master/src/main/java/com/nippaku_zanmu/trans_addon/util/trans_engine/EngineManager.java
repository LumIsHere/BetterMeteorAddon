package com.nippaku_zanmu.trans_addon.util.trans_engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class EngineManager {
    private static final EngineManager INSTANCE = new EngineManager();

    public static EngineManager getInstance() {
        return INSTANCE;
    }

    public Set<String> getEngineNames() {
        return engines.keySet();
    }

    public AbstractTransEngine getEngine(String name) {
        AbstractTransEngine engine = engines.get(name);
        return engine == null ? engines.get("OLD") : engine;
    }

    LinkedHashMap<String, AbstractTransEngine> engines = new LinkedHashMap<>();

    private EngineManager(){
        engines.put("OLD", new TransEngineOld());
        engines.put("NEW", new TransEngineNew());
    }

}
