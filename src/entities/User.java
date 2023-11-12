package entities;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

public class User implements DietaryPreferences, UserInventory, Iterable<FoodItem> {
    private PriorityQueue<FoodItem> inventory;
    private Map<String, Boolean> dietaryRestrictions;

    public User(Map<String, Boolean> dietaryRestrictions) {
        this.inventory = new PriorityQueue<>(new FoodItemComparator());
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public Map<String, Boolean> getDietaryRestrictions() {
        return this.dietaryRestrictions;
    }

    public void setDietaryRestrictions(Map<String, Boolean> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public void addItem(FoodItem item) {
        this.inventory.add(item);
    }

    public FoodItem removeItem() {
        return this.inventory.remove();
    }

    public boolean removeSpecificItem(FoodItem item) {
        return this.inventory.remove(item);
    }

    public void addRestriction(String restriction, Boolean res) {
        this.dietaryRestrictions.put(restriction, res);
    }

    public Boolean removeRestriction(String restriction, Boolean res) {
        return this.dietaryRestrictions.remove(restriction, res);
    }

    public PriorityQueue<FoodItem> getInventory() {
        return this.inventory;
    }

    private class FoodItemComparator implements Comparator<FoodItem> {
        @Override
        public int compare(FoodItem item1, FoodItem item2) {
            return item1.getCalendarObject().compareTo(item2.getCalendarObject());
        }
    }

    @Override
    public Iterator<FoodItem> iterator() {
        return new User.Iter();
    }

    private class Iter implements Iterator<FoodItem>{
        private int index = 0;
        private FoodItem[] temp = inventory.toArray(new FoodItem[inventory.size()]);

        @Override
        public boolean hasNext() {
            return index < inventory.size();
        }

        @Override
        public FoodItem next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return temp[index++];
        }
    }
}