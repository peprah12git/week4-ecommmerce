package com.service;

import com.models.Order;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MergeSortStrategy implements SortStrategy<Order> {
    private final Comparator<Order> comparator;

    public MergeSortStrategy(Comparator<Order> comparator) {
        this.comparator = comparator;
    }

    @Override
    public List<Order> sort(List<Order> items) {
        if (items.size() <= 1) return new ArrayList<>(items);
        return mergeSort(new ArrayList<>(items));
    }

    private List<Order> mergeSort(List<Order> list) {
        if (list.size() <= 1) return list;

        int mid = list.size() / 2;
        List<Order> left = mergeSort(new ArrayList<>(list.subList(0, mid)));
        List<Order> right = mergeSort(new ArrayList<>(list.subList(mid, list.size())));

        return merge(left, right);
    }

    private List<Order> merge(List<Order> left, List<Order> right) {
        List<Order> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }

        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));

        return result;
    }
}
