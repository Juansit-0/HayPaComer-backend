package dev.haypacomer.application.market;

import dev.haypacomer.application.port.UnitOfWork;
import java.util.function.Supplier;

public final class DirectWork implements UnitOfWork {

  @Override
  public <T> T run(Supplier<T> work) {
    return work.get();
  }
}
