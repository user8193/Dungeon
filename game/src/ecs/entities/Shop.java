package ecs.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import configuration.ItemConfig;
import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AITools;
import ecs.items.ItemData;
import graphic.Animation;
import graphic.hud.InputMenu;
import starter.Game;
import tools.Point;

import java.util.ArrayList;
import java.util.List;

public class Shop extends Entity{
    private List<String> inventoryItem = new ArrayList<>(4);
    private List<Integer> inventoryPrice = new ArrayList<>(4);
    private List<Integer> inventoryNumber = new ArrayList<>(4);
    private InputMenu inputMenu;
    private final String path = "animation/shopUI.png";
    private boolean inShop = false;
    private int newBuyValueManipulator = 0;
    private int newSellValueManipulator = 2;

    public Shop(){
        super();

        inventoryItem.add(0, "Cake");
        inventoryItem.add(1, "Bag");
        inventoryItem.add(2, "Speed");
        inventoryItem.add(3, "Monster");

        for(int x = 0; x < inventoryItem.size(); x++){
            int random = (int) (Math.random() * 7) + 3;
            inventoryPrice.add(random);
            random = (int) (Math.random() * 10);
            inventoryNumber.add(random);
        }

        setupAnimationComponent();
        setupPositionComponent();
    }

    private void setupAnimationComponent(){
        new AnimationComponent(this, AnimationBuilder.buildAnimation(path));
    }

    private void setupPositionComponent(){
        new PositionComponent(this);
    }

    public void shopping(){
        if(inputMenu == null){
            inputMenu = Game.inputMenu;
        }
        else if(inputMenu.result() == null){
            return;
        }
        else {
            if(inputMenu.result().matches(".*Shop.*") && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
                inShop = true;
                String s = "";
                for(int x = 0; x < inventoryItem.size(); x++){
                    inputMenu.removeOutputText();
                    s = s + "The " + inventoryItem.get(x) + " costs " + inventoryPrice.get(x).byteValue() + ". " + "Only " + inventoryNumber.get(x).byteValue() + " are available.\n";
                }
                inputMenu.createOutput(s);
            }
            if(inShop) {
                for (int x = 0; x < inventoryItem.size(); x++) {
                    if (inputMenu.result().matches(".*buy\\s" + inventoryItem.get(x) + ".*\\d") && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                        char c = inputMenu.result().charAt(inputMenu.result().length() - 1);
                        int value = Character.getNumericValue(c);
                        if (value < inventoryPrice.get(x)) {
                            int random = (int) (Math.random() * (7-newBuyValueManipulator)) + newBuyValueManipulator + 3;
                            if(newBuyValueManipulator < 6){
                                newBuyValueManipulator++;
                            }
                            inventoryPrice.set(x, random);
                            inputMenu.removeOutputText();
                            inputMenu.createOutput("You want to pay less. I will make you a new offer.\nEver you want to get an Item cheaper, the price will be more likely to be higher.\n New Price: " + inventoryPrice.get(x));
                            break;
                        }
                        else{
                            if(inventoryNumber.get(x) > 0 && Game.getHero().isPresent() && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
                                if(Game.getHero().get().getComponent(WalletComponent.class).isPresent()){
                                    WalletComponent wc = (WalletComponent) Game.getHero().get().getComponent(WalletComponent.class).get();
                                    int coins = wc.getCoins();
                                    if(coins > inventoryPrice.get(x)) {
                                        wc.removeCoins(inventoryPrice.get(x));
                                        switch (inventoryItem.get(x)) {
                                            case "Cake" -> new Cake();
                                            case "Bag" -> new Bag();
                                            case "Speed" -> new SpeedPotion();
                                            case "Monster" -> new MonsterPotion();
                                        }
                                        inventoryNumber.set(x, inventoryNumber.get(x) - 1);
                                    }
                                    else{
                                        inputMenu.removeOutputText();
                                        inputMenu.createOutput("You need to find more Coins");
                                    }
                                }
                            }
                        }
                    } else if (inputMenu.result().matches(".*sell\\s" + inventoryItem.get(x) + ".*\\d") && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                        char c = inputMenu.result().charAt(inputMenu.result().length() - 1);
                        int value = Character.getNumericValue(c);
                        if (value > inventoryPrice.get(x) - newSellValueManipulator) {
                            newSellValueManipulator = 1;
                            inputMenu.removeOutputText();
                            inputMenu.createOutput("There will be only one Offer now: " + (inventoryPrice.get(x)-1));
                            break;
                        }
                        else {
                            if(inventoryNumber.get(x) > 0 && Game.getHero().isPresent() && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
                                if (Game.getHero().get().getComponent(InventoryComponent.class).isEmpty()) {
                                    return;
                                }
                                else {
                                    InventoryComponent ic = (InventoryComponent) Game.getHero().get().getComponent(InventoryComponent.class).get();
                                    List<ItemData> items = ic.getItems();
                                    if(items == null || items.size() == 0){
                                        return;
                                    }
                                    else {
                                        if(Game.getHero().get().getComponent(WalletComponent.class).isPresent()) {
                                            WalletComponent wc = (WalletComponent) Game.getHero().get().getComponent(WalletComponent.class).get();
                                            wc.addCoin(inventoryPrice.get(x));
                                            switch (inventoryItem.get(x)) {
                                                case "Cake" -> sellItem(items, ic, ItemConfig.KUCHEN_NAME.get());
                                                case "Bag" -> sellItem(items, ic, ItemConfig.BAG_NAME.get());
                                                case "Speed" -> sellItem(items, ic, ItemConfig.SPEED_NAME.get());
                                                case "Monster" -> sellItem(items, ic, ItemConfig.MONSTER_DESPAWN_NAME.get());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void sellItem(List<ItemData> items, InventoryComponent ic, String name){
        for(int x = 0; x < items.size(); x++){
            if(items.get(x).getItemName().matches(name)){
                ic.removeItemAndItemInBag(items.get(x));
                break;
            }
            else if(items.get(x).getItemName().matches(ItemConfig.BAG_NAME.get())){
                for(int y = 0; y < items.get(x).getInventory().size(); y++){
                    if(items.get(x).getItemName().matches(name)){
                        ic.removeItemAndItemInBag(items.get(x));
                        break;
                    }
                }
            }
        }
        newSellValueManipulator = 2;
    }
}
