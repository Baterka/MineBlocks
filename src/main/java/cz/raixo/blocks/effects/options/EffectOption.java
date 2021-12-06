package cz.raixo.blocks.effects.options;

import cz.raixo.blocks.commands.MainCommand;
import cz.raixo.blocks.menu.utils.ConversationUtil;
import cz.raixo.blocks.util.NumberUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.Consumer;

public interface EffectOption {

    static void chatInput(Type type, Player player, Consumer<String> data) {
        switch (type) {
            case COLOR: {
                MainCommand.message(player, "Please type RGB color values!");
                MainCommand.message(player, "Please type red value!");
                ConversationUtil.getInstance().createConversation(player, new ConversationUtil.ConversationListener() {

                    int r = -1, g = -1, b = -1;

                    @Override
                    public boolean onMessage(String message) {
                        Optional<Integer> integerOptional = NumberUtil.parseInt(message);
                        if (integerOptional.isEmpty() || integerOptional.orElse(-1) < 0 || integerOptional.orElse(256) > 255) {
                            MainCommand.error(player, "Invalid number! If you want to exit this prompt, type 'exit'");
                        } else {
                            if (r == -1) {
                                r = integerOptional.get();
                            } else if (g == -1) {
                                g = integerOptional.get();
                            } else if (b == -1) {
                                this.b = integerOptional.get();
                                data.accept(r + "," + g + "," + b);
                                return false;
                            }
                        }
                        if (r == -1) {
                            MainCommand.message(player, "Please type red value!");
                        } else if (g == -1) {
                            MainCommand.message(player, "Please type green value!");
                        } else if (b == -1) {
                            MainCommand.message(player, "Please type blue value!");
                        }
                        return true;
                    }

                    @Override
                    public void onExit() {
                        if (r == -1 || g == -1 || b == -1) {
                            data.accept(null);
                        }
                    }
                });
                break;
            }
            case FLOAT: {
                MainCommand.message(player, "Please type decimal number!");
                ConversationUtil.getInstance().createConversation(player, new ConversationUtil.ConversationListener() {

                    private float input = -1;

                    @Override
                    public boolean onMessage(String message) {
                        Optional<Float> floatOptional = NumberUtil.parseFloat(message);
                        if (floatOptional.isEmpty()) {
                            MainCommand.error(player, "Invalid number! If you want to exit this prompt, type 'exit'");
                        } else {
                            this.input = floatOptional.get();
                            data.accept(String.valueOf(input));
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public void onExit() {
                        if (input == -1) {
                            data.accept(null);
                        }
                    }
                });
                break;
            }
            case INTEGER: {
                MainCommand.message(player, "Please type number!");
                ConversationUtil.getInstance().createConversation(player, new ConversationUtil.ConversationListener() {

                    private int input = -1;

                    @Override
                    public boolean onMessage(String message) {
                        Optional<Integer> integerOptional = NumberUtil.parseInt(message);
                        if (integerOptional.isEmpty()) {
                            MainCommand.error(player, "Invalid number! If you want to exit this prompt, type 'exit'");
                        } else {
                            this.input = integerOptional.get();
                            data.accept(String.valueOf(input));
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public void onExit() {
                        if (input == -1) {
                            data.accept(null);
                        }
                    }
                });
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid type argument");
            }
        }
    }

    static String toReadable(Type type, String fromInput) {
        switch (type) {
            case COLOR: {
                String[] args = fromInput.replace(" ", "").split(",", 4);
                if (args.length < 3) {
                    return fromInput;
                }
                Optional<Integer> r = NumberUtil.parseInt(args[0]);
                Optional<Integer> g = NumberUtil.parseInt(args[1]);
                Optional<Integer> b = NumberUtil.parseInt(args[2]);
                if (r.isEmpty() || g.isEmpty() || b.isEmpty()) {
                    return fromInput;
                }
                Color color = Color.fromRGB(r.get(), g.get(), b.get());
                return color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + String.format(" <#%02x%02x%02x>\u2588", color.getRed(), color.getGreen(), color.getBlue());
            }
            default: {
                return fromInput;
            }
        }
    }

    Type getType();

    String getName();

    enum Type {
        INTEGER(Material.SNOW_BLOCK),
        FLOAT(Material.CLAY_BALL),
        COLOR(Material.BLUE_DYE);

        private final Material icon;

        Type(Material icon) {
            this.icon = icon;
        }

        public Material getIcon() {
            return icon;
        }
    }

}
