package plugin.treasurehunt.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import plugin.treasurehunt.Main;

/** 制限時間ないにランダムで出現する生物を倒して、スコアを獲得するゲームを起動するコマンドです。
 * スコアは生物によって変わり、倒せた生物の合計によってスコアが変動します。
 * 結果はプレイヤー名、日時、合計点数を表示します。
 */

public class TreasureHuntCommand extends BaseCommand implements Listener {
  private Main main;
  private  Player player;
  private List<Entity> spawnEntityList = new ArrayList<>();
  private int gameTime = 70;
  private  int score;
  public TreasureHuntCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player) {
    this.player = player;
    gameTime = 70;
    score = 0;

    gamePlay(player);
    return true;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender) {
    return false;
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent e) {
    LivingEntity entity = e.getEntity();
    Player player = e.getEntity().getKiller();

    if(Objects.isNull(player) || spawnEntityList.stream().noneMatch(Entity -> Entity.equals(entity))){
      return;
    }

    if(this.player.getName().equals(player.getName())) {
      int point = switch (entity.getType()) {
        case SHEEP -> -10;
        case PIG -> (gameTime <= 60) ? 100 : (gameTime <= 70) ? 50 : 0;
        case LLAMA -> 200;
        default -> 0;
      };

      score +=point;
      player.sendMessage("生物を倒した！現在のスコアは" + score + "点！");
    }
  }

  private void gamePlay(Player player) {
    player.sendTitle("宝探しゲームスタート！",
        "豚・ラマを倒して点数ゲット！",
        0,60,0);

    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if(gameTime <= 0) {
        Runnable.cancel();
        player.sendTitle("ゲームが終了しました。",
            player.getName() + "合計" + score + "点！",
            0,60,0);

        spawnEntityList.forEach(Entity::remove);
        spawnEntityList = new ArrayList<>();
        return;
      }
      Entity spawnEntity = player.getWorld().spawnEntity(getEntitySpawnLocation(player), getEntity());
      spawnEntityList.add(spawnEntity);
      gameTime -= 20;
    }, 0,20 * 20);
  }

  /**
   * 生物の出現場所を取得します。
   * 出現エリアはX軸とZ軸は自分の位置からプラス、ランダムで−20〜29の値が設定されます。
   * Y軸はプレイヤーと同じ位置になります。
   *
   * @param player　コマンドを実行したプレイヤー
   * @return　生物の出現場所
   */
  private Location getEntitySpawnLocation(Player player) {
    Location playerLocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(30) - 20;
    int randomZ = new SplittableRandom().nextInt(30) - 20;

    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    return new Location(player.getWorld(), x, y, z);
  }

  /**
   *ランダムで敵を抽選して、その結果の生物を取得する。
   * @return*　生物
   */
  private EntityType getEntity() {
    List<EntityType> entityList = List.of(EntityType.SHEEP, EntityType.PIG, EntityType.LLAMA);
    return entityList.get(new SplittableRandom().nextInt(entityList.size()));
  }
}