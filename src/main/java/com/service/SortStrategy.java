package com.service;

import java.util.List;

public interface SortStrategy<T> {
    List<T> sort(List<T> items);
}
