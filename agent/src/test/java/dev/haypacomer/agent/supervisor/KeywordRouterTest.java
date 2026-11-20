package dev.haypacomer.agent.supervisor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haypacomer.agent.runtime.AgentTask;
import java.util.List;
import org.junit.jupiter.api.Test;

class KeywordRouterTest {

  private final KeywordRouter router = new KeywordRouter();

  @Test
  void onlyTheCurrentMessageIsRouted() {
    String earlier = AgentTask.EARLIER + "user: agrega arroz a la lista\nassistant: listo";

    assertEquals(List.of(Specialist.CHEF), router.route("¿qué se vence pronto?" + earlier));
    assertEquals(List.of(Specialist.CHEF), router.route("hola" + earlier));
    assertEquals(
        List.of(Specialist.CHEF),
        router.route("¿qué tengo en la nevera?" + AgentTask.EARLIER + "user: la puerta abierta"));
  }

  @Test
  void theFridgeIsNotTheColdSpecialistUnlessItIsAboutCold() {
    assertEquals(List.of(Specialist.CHEF), router.route("¿qué hay en la nevera?"));
    assertEquals(List.of(Specialist.COLD), router.route("dejaron la puerta abierta"));
    assertEquals(List.of(Specialist.COLD), router.route("la temperatura de la nevera"));
  }

  @Test
  void anOrderGoesToTheSpecialistWithItsTool() {
    assertEquals(List.of(Specialist.CHEF), router.route("boté el tomate"));
    assertEquals(List.of(Specialist.CHEF), router.route("compré 2 kg de arroz"));
    assertEquals(List.of(Specialist.CHEF), router.route("siguiente paso"));
    assertEquals(List.of(Specialist.MARKET), router.route("marca el arroz como comprado"));
    assertEquals(List.of(Specialist.MARKET), router.route("presupuesto de 300000"));
    assertEquals(List.of(Specialist.MARKET), router.route("qué me falta para arroz con pollo"));
    assertEquals(List.of(Specialist.MARKET), router.route("pasa el plan al mercado"));
    assertEquals(List.of(Specialist.CHEF), router.route("haz el plan de la semana"));
  }
}
