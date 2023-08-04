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
package net.kaw.dev.corico.remote.discord;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.kaw.dev.corico.remote.App;

public class Configuration {

    private String botToken;
    private String ownerId;

    public Configuration() {
        this.botToken = "BOT_TOKEN_HERE";
        this.ownerId = "OWNER_ID_HERE";
    }

    public Configuration(String botToken, String ownerId) {
        this.botToken = botToken;
        this.ownerId = ownerId;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void load(String filepath) throws IllegalStateException, JsonSyntaxException, FileNotFoundException {
        Configuration config = App.gson.fromJson(new JsonReader(new FileReader(filepath)), Configuration.class);

        this.botToken = config.getBotToken();
        this.ownerId = config.getOwnerId();
    }

    public void save(String filepath) {
        try {
            String json = App.gson.toJson(this);

            Path path = Paths.get(filepath);

            Files.write(path, json.getBytes());
        } catch (IOException ex) {
        }
    }

    public static void saveDefault(String filepath) {
        try {
            Path path = Paths.get(filepath);

            Files.createFile(path);

            Configuration defaultConfig = new Configuration("BOT_TOKEN_HERE", "OWNER_ID_HERE");

            String json = App.gson.toJson(defaultConfig);

            Files.write(path, json.getBytes());
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException ex) {
        }
    }

    @Override
    public String toString() {
        return "Configuration{" + "botToken=" + botToken + ", ownerId=" + ownerId + '}';
    }

}
