package dev.blu3.pokebuilder.config.types;

import java.util.HashMap;

import static dev.blu3.pokebuilder.utils.Utils.getNewSpecificPrices;


public class PokeBuilderPricesConfig {
    public HashMap<String, HashMap<String, Integer>> specificPrices = getNewSpecificPrices();
    public double priceMultiplier = 1.5;
    public int shinyCost = 1000;
    public int unsetShinyCost = 100;
    public int natureCost = 50;
    public int oneLevelCost = 2;
    public int fiveLevelCost = 10;
    public int tenLevelCost = 20;
    public int ballCost = 20;
    public int genderCost = 50;
    public int abilityCost = 350;
    public int hiddenAbilityCost = 1750;
//    public int breedLockCost = 5;
    public int maxIVCost = 18500;
    public int oneIVIncCost = 100;
    public int tenIVIncCost = 1000;
    public int twentyFiveIVIncCost = 2500;
    public int oneIVDecCost = 75;
    public int tenIVDecCost = 750;
    public int twentyFiveIVDecCost = 1875;
    public int oneEVIncCost = 1;
    public int tenEVIncCost = 10;
    public int twentyFiveEVIncCost = 25;
    public int oneEVDecCost = 2;
    public int tenEVDecCost = 20;
    public int twentyFiveEVDecCost = 50;
}
