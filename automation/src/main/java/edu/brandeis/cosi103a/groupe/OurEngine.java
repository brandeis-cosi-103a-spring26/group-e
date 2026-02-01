package edu.brandeis.cosi103a.groupe;

import edu.brandeis.cosi.atg.engine.Engine;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;

public class OurEngine implements Engine {
    public GameResult play() throws PlayerViolationException {
        return new GameResult(null);
    }
}