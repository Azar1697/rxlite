# RxJava-Lite

## 1. Архитектура
* **Observable** хранит `OnSubscribe`, операторы создают новые Observable без сайд-эффектов (ленивая цепочка).
* **Observer** — три метода, паттерн «Наблюдатель».
* **Disposable / Composite** — отмена подписок, освобождение ресурсов.
* **Schedulers** инкапсулируют пул потоков; `subscribeOn` переносит работу источника, `observeOn` — доставку событий.

## 2. Schedulers
| Класс | Пул | Когда применять |
|-------|-----|-----------------|
| `IOThreadScheduler` | `Executors.newCachedThreadPool()` | сетевые / I/O операции |
| `ComputationScheduler` | `Executors.newFixedThreadPool(CPU)` | CPU-bound задачи |
| `SingleThreadScheduler` | `Executors.newSingleThreadExecutor()` | UI-подобные, последовательные операции |

## 3. Реализованные операторы
* `map`, `filter`, `flatMap`
* Потоковые операторы `subscribeOn`, `observeOn`

## 4. Тестирование
* JUnit 5, запуск `mvn clean verify`
* Покрыты сценарии:
  * трансформация и фильтрация (`map`, `filter`);
  * композиция потоков (`flatMap`);
  * обработка ошибок (единственный вызов `onError`, после него `onNext` не приходит);
  * корректная работа Schedulers — события приходят из разных потоков.

## 5. Пример использования

```java
Scheduler io  = new IOThreadScheduler();
Scheduler ui  = new SingleThreadScheduler();

Observable.just(1)
          .subscribeOn(io)
          .map(i -> i * 2)
          .filter(i -> i > 1)
          .observeOn(ui)
          .subscribe(new Observer<>() {
              public void onNext(Integer i) { System.out.println("UI got " + i); }
              public void onError(Throwable t) { t.printStackTrace(); }
              public void onComplete() { System.out.println("Done"); }
          });
```

## 6. Логи mvn clean verify

![image](https://github.com/user-attachments/assets/5c9850c5-f564-4555-a223-6bccf5d38281)
![image](https://github.com/user-attachments/assets/3014adc8-f155-4222-b75f-cda959d287e2)

