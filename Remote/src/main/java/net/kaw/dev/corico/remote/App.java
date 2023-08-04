/*
 * MIT License
 * 
 * Copyright (c) 2023 Kawtious
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.kaw.dev.corico.remote;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.mattco98.voicemeeter.Voicemeeter;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.kaw.dev.corico.remote.discord.Bot;
import net.kaw.dev.corico.remote.discord.Configuration;

public class App {

    public static final Gson gson = new Gson();

    public static void main(String[] args) {
        System.out.println("Hello World!");

        Voicemeeter.init(true);

        Voicemeeter.login();

        Configuration.saveDefault("./config.json");

        try {
            Configuration config = new Configuration();
            config.load("./config.json");

            Bot bot = new Bot(config);
        } catch (IllegalStateException | JsonSyntaxException | FileNotFoundException | InvalidTokenException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
