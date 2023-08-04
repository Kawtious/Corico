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

public enum Tasks {

    TF2_SERVER("cmd", "/c", "\"D:\\Games\\SteamCMD\\steamapps\\common\\Team Fortress 2 Dedicated Server\\srcds.exe\"", "-console", "-game tf", "+map pl_badwater", "+maxplayers 24", "-insecure");

    private final String[] commands;

    private Tasks(String... commands) {
        this.commands = commands;
    }

    public String command() {
        StringBuilder sb = new StringBuilder();

        for (String command : commands) {
            sb.append(command).append(" ");
        }

        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
