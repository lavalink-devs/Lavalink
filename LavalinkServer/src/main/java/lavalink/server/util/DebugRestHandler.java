/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

@RestController
public class DebugRestHandler {

    private static final Logger log = LoggerFactory.getLogger(DebugRestHandler.class);
    private ScriptEngine engine;

    public DebugRestHandler() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval("var imports = new JavaImporter(java.io, java.lang, java.util);");
        } catch (ScriptException ex) {
            log.error("Failed setting up Nashorn", ex);
        }
    }

    @PostMapping(value = "/debug/nashorn")
    @ResponseBody
    public String getLoadTracks(HttpServletRequest request, HttpServletResponse response,
                                @RequestHeader String authorization, @RequestBody String body) {
        if (!checkAuth(authorization)) {
            response.setStatus(403);
            log.warn("Authentication failed from {}. Attempted code:\n{}", request.getRemoteHost(), body);
            return "Not authorized";
        }

        Object out;
        try {
            out = engine.eval(
                    "(function() {"
                            + "with (imports) {\n" + body + "\n}"
                            + "})();");

        } catch (Exception ex) {
            log.info("Error occurred in eval", ex);
            return ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }

        if (out == null) return "null";

        return out.toString();
    }

    private boolean checkAuth(String token) {
        File file = new File("debug_token");
        String realToken;
        try (Scanner scanner = new Scanner(new FileInputStream(file))) {
            realToken = scanner
                    .nextLine()
                    .trim();
        } catch (FileNotFoundException e) {
            log.error("Debug token not found", e);
            return false;
        }

        if (realToken.length() < 8) throw new RuntimeException("Debug token is very short. Config error?");

        return realToken.equals(token);
    }

}
