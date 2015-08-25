package com.joker.supportdesign.mvp.model;

import com.joker.supportdesign.mvp.domain.Animal;
import com.joker.supportdesign.mvp.domain.event.ItemEvent;
import com.joker.supportdesign.util.EventBusInstance;
import de.greenrobot.event.EventBus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Joker on 2015/6/29.
 */
public class FragmentInteractorImp implements FragmentInteractor {

  private static final String baseUrl = "http://lorempixel.com/500/500/animals/";
  private static final int itemCount = 16;

  private List<Animal> dataList = new ArrayList(itemCount);
  private Random random = new Random();

  private EventBus eventBus;

  public FragmentInteractorImp() {

    eventBus = EventBusInstance.getDefault();
  }

  @Override public void getData() {

    Animal animal = new Animal();

    for (int i = 0; i < itemCount; i++) {

      Animal clone = animal.newInstance();
      clone.setUrl(baseUrl + random.nextInt(10));
      clone.setName(FragmentInteractorImp.this.createArrayList().get(i));
      dataList.add(clone);
    }

    if (eventBus.hasSubscriberForEvent(ItemEvent.class)) {
      eventBus.post(new ItemEvent(dataList));
    }
  }

  private List<String> createArrayList() {
    return Arrays.asList("Item 0", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6",
        "Item 7", "Item 8", "Item 9", "Item 10", "Item 11", "Item 12", "Item 13", "Item 14",
        "Item 15");
  }
}
