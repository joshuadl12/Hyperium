/*
 *       Copyright (C) 2018-present Hyperium <https://hyperium.cc/>
 *
 *       This program is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published
 *       by the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       This program is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.hyperium.mods.keystrokes.config;

import cc.hyperium.Hyperium;
import cc.hyperium.mods.keystrokes.KeystrokesMod;
import cc.hyperium.mods.keystrokes.keys.impl.CustomKey;
import cc.hyperium.mods.keystrokes.render.CustomKeyWrapper;
import cc.hyperium.utils.BetterJsonObject;
import cc.hyperium.utils.JsonHolder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class KeystrokesSettings {

    private final KeystrokesMod theMod;
    private final File configFile;
    private int x = 0;
    private int y = 0;
    private boolean enabled = true;
    private boolean chroma = false;
    private boolean mouseButtons = false;
    private boolean showCPS = false;
    private boolean showCPSOnButtons = false;
    private boolean showSpacebar = false;
    private double scale = 1;
    private double fadeTime = 1;
    private int red = 255;
    private int green = 255;
    private int blue = 255;
    private int pressedRed = 0;
    private int pressedGreen = 0;
    private int pressedBlue = 0;
    private boolean leftClick = true;
    private boolean showingSneak = false;
    private boolean showingFPS = false;
    private boolean keyBackground = true;
    private List<CustomKeyWrapper> configWrappers = new ArrayList<>();

    public KeystrokesSettings(KeystrokesMod mod, File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }

        this.theMod = mod;

        this.configFile = new File(directory, "keystrokes.json");
    }

    public void load() {
        try {
            if (!this.configFile.getParentFile().exists() || !this.configFile.exists()) {
                save();
                return;
            }

            BufferedReader f = new BufferedReader(new FileReader(this.configFile));
            List<String> options = f.lines().collect(Collectors.toList());
            StringBuilder builder = new StringBuilder();

            if (options.isEmpty()) {
                return;
            }

            for (String s : options) {
                builder.append(s);
            }

            if (builder.toString().trim().length() > 0) {
                parseSettings(new BetterJsonObject(builder.toString().trim()));
            }
        } catch (Exception ex) {
            Hyperium.LOGGER.warn(String.format("Could not load config file! (\"%s\")", this.configFile.getName()));
            save();
        }
    }

    public void save() {
        try {
            if (!this.configFile.getParentFile().exists()) {
                this.configFile.getParentFile().mkdirs();
            }

            if (!this.configFile.exists()) {
                if (!this.configFile.createNewFile()) {
                    return;
                }
            }

            BetterJsonObject object = new BetterJsonObject();
            object.addProperty("x", x);
            object.addProperty("y", y);
            object.addProperty("leftClick", leftClick);
            object.addProperty("red", red);
            object.addProperty("green", green);
            object.addProperty("blue", blue);
            object.addProperty("pressedRed", pressedRed);
            object.addProperty("pressedGreen", pressedGreen);
            object.addProperty("pressedBlue", pressedBlue);
            object.addProperty("scale", getScale());
            object.addProperty("fadeTime", getFadeTime());
            object.addProperty("enabled", enabled);
            object.addProperty("chroma", chroma);
            object.addProperty("mouseButtons", mouseButtons);
            object.addProperty("showCPS", showCPS);
            object.addProperty("showCPSOnButtons", showCPSOnButtons);
            object.addProperty("showSpacebar", showSpacebar);
            object.addProperty("showSneak", showingSneak);
            object.addProperty("showFps", showingFPS);
            object.addProperty("keyBackground", keyBackground);
            final JsonArray keys = new JsonArray();
            for (CustomKeyWrapper wrapper : theMod.getRenderer().getCustomKeys()) {
                JsonHolder holder = new JsonHolder();
                holder.put("key", wrapper.getKey().getKey());
                holder.put("type", wrapper.getKey().getType());
                holder.put("xOffset", wrapper.getxOffset());
                holder.put("yOffset", wrapper.getyOffset());
                keys.add(holder.getObject());
            }

            object.getData().add("custom", keys);
            object.writeToFile(configFile);
        } catch (Exception ex) {
            Hyperium.LOGGER.warn(String.format("Could not save config file! (\"%s\")", this.configFile.getName()));
        }
    }

    private void parseSettings(BetterJsonObject object) {
        x = object.optInt("x");
        y = object.optInt("y");
        red = object.optInt("red", 255);
        green = object.optInt("green", 255);
        blue = object.optInt("blue", 255);
        pressedRed = object.optInt("pressedRed");
        pressedGreen = object.optInt("pressedGreen");
        pressedBlue = object.optInt("pressedBlue");
        setScale(object.optDouble("scale", 1.0D));
        setFadeTime(object.optDouble("fadeTime", 1.0D));
        enabled = object.optBoolean("enabled", true);
        chroma = object.optBoolean("chroma");
        leftClick = object.optBoolean("leftClick", true);
        mouseButtons = object.optBoolean("mouseButtons");
        showCPS = object.optBoolean("showCPS");
        showCPSOnButtons = object.optBoolean("showCPSOnButtons");
        showSpacebar = object.optBoolean("showSpacebar");
        showingSneak = object.optBoolean("showSneak");
        showingFPS = object.optBoolean("showFps");
        keyBackground = object.optBoolean("keyBackground");
        JsonObject data = object.getData();
        if (data.has("custom")) {
            JsonArray custom = data.getAsJsonArray("custom");
            for (JsonElement element : custom) {
                JsonHolder holder = new JsonHolder(element.getAsJsonObject());
                CustomKeyWrapper wrapper = new CustomKeyWrapper(
                    new CustomKey(theMod, holder.optInt("key"), holder.optInt("type")), holder.optInt("xOffset"), holder.optInt("yOffset"));
                configWrappers.add(wrapper);
            }
        }
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRed() {
        return this.red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return this.green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return this.blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getPressedRed() {
        return this.pressedRed;
    }

    public void setPressedRed(int red) {
        this.pressedRed = red;
    }

    public int getPressedGreen() {
        return this.pressedGreen;
    }

    public void setPressedGreen(int green) {
        this.pressedGreen = green;
    }

    public int getPressedBlue() {
        return this.pressedBlue;
    }

    public void setPressedBlue(int blue) {
        this.pressedBlue = blue;
    }

    public double getScale() {
        return capDouble(this.scale, 0.5F, 1.5F);
    }

    public void setScale(double scale) {
        this.scale = capDouble(scale, 0.5F, 1.5F);
    }

    public double getFadeTime() {
        return capDouble(this.fadeTime, 0.1F, 3.0F);
    }

    public void setFadeTime(double scale) {
        this.fadeTime = capDouble(scale, 0.1F, 3.0F);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isShowingMouseButtons() {
        return this.mouseButtons;
    }

    public void setShowingMouseButtons(boolean showingMouseButtons) {
        this.mouseButtons = showingMouseButtons;
    }

    public boolean isShowingSpacebar() {
        return this.showSpacebar;
    }

    public void setShowingSpacebar(boolean showSpacebar) {
        this.showSpacebar = showSpacebar;
    }

    public boolean isShowingCPS() {
        return this.showCPS;
    }

    public void setShowingCPS(boolean showingCPS) {
        this.showCPS = showingCPS;
    }

    public boolean isShowingCPSOnButtons() {
        return this.showCPSOnButtons;
    }

    public void setShowingCPSOnButtons(boolean showCPSOnButtons) {
        this.showCPSOnButtons = showCPSOnButtons;
    }

    public boolean isChroma() {
        return this.chroma;
    }

    public void setChroma(boolean showingChroma) {
        this.chroma = showingChroma;
    }

    public boolean isLeftClick() {
        return this.leftClick;
    }

    public void setLeftClick(boolean leftClick) {
        this.leftClick = leftClick;
    }

    public boolean isShowingFPS() {
        return showingFPS;
    }

    public void setShowingFPS(boolean showingFPS) {
        this.showingFPS = showingFPS;
    }

    public boolean isKeyBackgroundEnabled() {
        return keyBackground;
    }

    public void setKeyBackgroundEnabled(boolean keyBackground) {
        this.keyBackground = keyBackground;
    }

    public int getHeight() {
        int height = 50;

        if (showCPS) {
            height += 18;
        }

        if (mouseButtons) {
            height += 24;
        }

        if (showSpacebar) {
            height += 18;
        }

        return height;
    }

    public int getWidth() {
        return 74; // Hardcoded value
    }

    public List<CustomKeyWrapper> getConfigWrappers() {
        return configWrappers;
    }

    public boolean isShowingSneak() {
        return showingSneak;
    }

    public void setShowingSneak(boolean showingSneak) {
        this.showingSneak = showingSneak;
    }

    public KeystrokesMod getMod() {
        return this.theMod;
    }

    private float capFloat(float valueIn, float minValue, float maxValue) {
        return (valueIn < minValue) ? minValue : ((valueIn > maxValue) ? maxValue : valueIn);
    }

    private double capDouble(double valueIn, double minValue, double maxValue) {
        return (valueIn < minValue) ? minValue : ((valueIn > maxValue) ? maxValue : valueIn);
    }

    private int capInt(int valueIn, int minValue, int maxValue) {
        return (valueIn < minValue) ? minValue : ((valueIn > maxValue) ? maxValue : valueIn);
    }
}
