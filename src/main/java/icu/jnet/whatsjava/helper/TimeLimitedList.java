package icu.jnet.whatsjava.helper;

import java.util.*;

public class TimeLimitedList<E> extends ArrayList<E> {

    private final HashMap<Integer, Long> hashMap = new HashMap<>();

    @Override
    public boolean add(E e) {
        removeOldEntries();
        hashMap.put(size(), System.currentTimeMillis());
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        removeOldEntries();
        for(int i = 0; i < c.size(); i++) {
            hashMap.put(size(), System.currentTimeMillis());
        }
        return super.addAll(c);
    }

    private void removeOldEntries() {
        long now = System.currentTimeMillis();

        // Get the iterator from entry set
        Set<Map.Entry<Integer, Long>> entrySet = hashMap.entrySet();
        Iterator<Map.Entry<Integer, Long>> iterator = entrySet.iterator();

        // Iterate over map and remove outdated entries
        while(iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            long timestamp = entry.getValue();

            // Delete entries older than 200 millis
            if(now - timestamp > 200) {
                iterator.remove();
                remove(entry.getKey());
            }
        }
    }
}
