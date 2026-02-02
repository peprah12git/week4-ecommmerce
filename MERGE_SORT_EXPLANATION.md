# Merge Sort with Strategy Pattern - Explanation

## 📋 Overview
We implemented **Merge Sort** using the **Strategy Design Pattern** to make sorting flexible and maintainable.

---

## 🏗️ Architecture Flow

```
┌─────────────────┐
│  OrderService   │  ← Client that needs sorting
└────────┬────────┘
         │ uses
         ▼
┌─────────────────┐
│ SortStrategy<T> │  ← Interface (contract)
└────────┬────────┘
         │ implements
         ▼
┌─────────────────┐
│ MergeSortStrategy│ ← Concrete implementation
└─────────────────┘
```

---

## 🔍 Component Breakdown

### 1️⃣ **SortStrategy Interface** (The Contract)
```java
public interface SortStrategy<T> {
    List<T> sort(List<T> items);
}
```

**Why it exists:**
- Defines a **contract** that all sorting algorithms must follow
- Uses **generics (`<T>`)** so it works with any type (Order, Product, User, etc.)
- Allows swapping algorithms without changing client code

**Real-world analogy:** 
Like a "payment method" interface - whether you use Credit Card, PayPal, or Cash, the checkout process stays the same.

---

### 2️⃣ **MergeSortStrategy** (The Implementation)

#### Constructor - Receives the Comparator
```java
private final Comparator<Order> comparator;

public MergeSortStrategy(Comparator<Order> comparator) {
    this.comparator = comparator;  // ← Stores HOW to compare
}
```

**Why pass Comparator?**
- Merge sort needs to know **how to compare** two items
- Same algorithm can sort by date, amount, name, etc.
- **Separation of concerns**: Algorithm doesn't care WHAT it sorts, just HOW

---

#### The sort() Method - Entry Point
```java
@Override
public List<Order> sort(List<Order> items) {
    if (items.size() <= 1) return new ArrayList<>(items);
    return mergeSort(new ArrayList<>(items));
}
```

**What happens:**
1. Checks if list is already sorted (0 or 1 item)
2. Creates a **copy** to avoid modifying original
3. Calls recursive mergeSort

---

#### mergeSort() - The Divide Step
```java
private List<Order> mergeSort(List<Order> list) {
    if (list.size() <= 1) return list;  // Base case

    int mid = list.size() / 2;
    List<Order> left = mergeSort(new ArrayList<>(list.subList(0, mid)));
    List<Order> right = mergeSort(new ArrayList<>(list.subList(mid, list.size())));

    return merge(left, right);
}
```

**Visual example with 4 orders:**
```
[Order1, Order2, Order3, Order4]
         ↓ Split
[Order1, Order2]  [Order3, Order4]
    ↓ Split           ↓ Split
[Order1] [Order2]  [Order3] [Order4]
    ↓ Merge           ↓ Merge
[Order1, Order2]  [Order3, Order4]
         ↓ Merge
[Order1, Order2, Order3, Order4] ← Sorted!
```

---

#### merge() - The Conquer Step
```java
private List<Order> merge(List<Order> left, List<Order> right) {
    List<Order> result = new ArrayList<>();
    int i = 0, j = 0;

    // Compare and merge
    while (i < left.size() && j < right.size()) {
        if (comparator.compare(left.get(i), right.get(j)) <= 0) {
            result.add(left.get(i++));
        } else {
            result.add(right.get(j++));
        }
    }

    // Add remaining items
    while (i < left.size()) result.add(left.get(i++));
    while (j < right.size()) result.add(right.get(j++));

    return result;
}
```

**Step-by-step merge example:**
```
Left:  [Order1($50), Order3($150)]
Right: [Order2($100), Order4($200)]

Comparing by amount (ascending):

Step 1: Compare $50 vs $100  → Add Order1  → Result: [Order1]
Step 2: Compare $150 vs $100 → Add Order2  → Result: [Order1, Order2]
Step 3: Compare $150 vs $200 → Add Order3  → Result: [Order1, Order2, Order3]
Step 4: Right exhausted      → Add Order4  → Result: [Order1, Order2, Order3, Order4]
```

**Key line:**
```java
if (comparator.compare(left.get(i), right.get(j)) <= 0)
```
- Uses the **Comparator** passed in constructor
- Returns: negative (left < right), 0 (equal), positive (left > right)

---

## 🔄 How It's Used in OrderService

### Example 1: Sort by Date (Ascending)
```java
public List<Order> sortByDate(boolean ascending) {
    // Step 1: Create the comparison rule
    Comparator<Order> comparator = ascending ? 
        Comparator.comparing(Order::getOrderDate) : 
        Comparator.comparing(Order::getOrderDate).reversed();
    
    // Step 2: Create strategy with that rule
    SortStrategy<Order> sortStrategy = new MergeSortStrategy(comparator);
    
    // Step 3: Execute the sort
    return sortStrategy.sort(getAllOrders());
}
```

**Flow diagram:**
```
OrderService.sortByDate(true)
    ↓
Creates: Comparator<Order> (by date, ascending)
    ↓
Passes to: new MergeSortStrategy(comparator)
    ↓
Calls: sortStrategy.sort(orders)
    ↓
MergeSortStrategy uses comparator to sort
    ↓
Returns: Sorted list
```

---

### Example 2: Sort by Amount (Descending)
```java
public List<Order> sortByAmount(boolean ascending) {
    Comparator<Order> comparator = ascending ? 
        Comparator.comparing(Order::getTotalAmount) : 
        Comparator.comparing(Order::getTotalAmount).reversed();
    
    SortStrategy<Order> sortStrategy = new MergeSortStrategy(comparator);
    return sortStrategy.sort(getAllOrders());
}
```

**Same algorithm, different comparison!**

---

## 🎯 Why This Design?

### ✅ Benefits

1. **Flexibility**
   ```java
   // Easy to add new sorting algorithms
   public class QuickSortStrategy implements SortStrategy<Order> { ... }
   public class HeapSortStrategy implements SortStrategy<Order> { ... }
   ```

2. **Reusability**
   ```java
   // Same strategy works for Products, Users, etc.
   SortStrategy<Product> productSort = new MergeSortStrategy<>(productComparator);
   ```

3. **Testability**
   ```java
   // Can test sorting independently
   @Test
   public void testMergeSort() {
       SortStrategy<Order> strategy = new MergeSortStrategy<>(comparator);
       List<Order> sorted = strategy.sort(testOrders);
       // Assert...
   }
   ```

4. **Single Responsibility**
   - OrderService: Business logic
   - MergeSortStrategy: Sorting algorithm
   - Comparator: Comparison logic

---

## 📊 Performance

**Merge Sort Characteristics:**
- **Time Complexity:** O(n log n) - guaranteed (best, average, worst)
- **Space Complexity:** O(n) - needs extra space for merging
- **Stable:** Yes - maintains relative order of equal elements

**Why choose Merge Sort?**
- Predictable performance (no worst-case O(n²) like QuickSort)
- Stable sorting (important for orders with same date/amount)
- Good for linked lists and external sorting

---

## 🔗 Data Flow Example

```
User clicks "Sort by Date ↑" in UI
    ↓
Controller calls: orderService.sortByDate(true)
    ↓
OrderService creates: Comparator.comparing(Order::getOrderDate)
    ↓
OrderService creates: new MergeSortStrategy(comparator)
    ↓
OrderService calls: sortStrategy.sort(getAllOrders())
    ↓
MergeSortStrategy:
    1. Divides list recursively
    2. Uses comparator.compare() to merge
    3. Returns sorted list
    ↓
OrderService returns sorted list to Controller
    ↓
UI displays sorted orders
```

---

## 💡 Key Takeaways

1. **Interface (SortStrategy)** = Contract that defines WHAT to do
2. **Implementation (MergeSortStrategy)** = Defines HOW to do it
3. **Comparator** = Passed as parameter to define comparison logic
4. **Strategy Pattern** = Allows swapping algorithms at runtime
5. **Generics** = Makes code reusable for any type

---

## 🚀 Future Extensions

```java
// Easy to add more strategies
public class TimSortStrategy implements SortStrategy<Order> { ... }
public class RadixSortStrategy implements SortStrategy<Order> { ... }

// Easy to add more comparators
Comparator<Order> byStatus = Comparator.comparing(Order::getStatus);
Comparator<Order> byUserThenDate = 
    Comparator.comparing(Order::getUserId)
              .thenComparing(Order::getOrderDate);
```

---

## 📝 Summary

**The pattern works like this:**
1. Define a **contract** (interface)
2. Implement the **algorithm** (merge sort)
3. Accept **comparison logic** (comparator) as parameter
4. Client uses the **strategy** without knowing implementation details

This is **dependency injection** + **strategy pattern** = Clean, flexible, maintainable code! 🎉
