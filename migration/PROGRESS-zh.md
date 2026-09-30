# Ripples of the Past 1.16.5 → 1.20.1 移植进展

> 本文是阶段性简报（最近更新：第 65 轮工作结束时）。
> 详细的任务清单、验收标准与逐轮记录见 [`migration/README.md`](README.md)。

## 1. 任务目标

把 **Ripples of the Past** 从 Minecraft **1.16.5 Forge** 迁移到 **1.20.1 Forge / Java 17**：

- 以 1.16.5 版为基底，1.21.1 版仅作参考；
- 保留全部原有功能 / 行为 / 内容 / 资源（不靠删除、注释或占位来通过编译）；
- 正确适配 registries、Capability、SimpleChannel、渲染、Mixin、worldgen、NBT、客户端-服务端同步；
- 分阶段迁移并持续构建，按系统逐个提交（仅本地 commit，不推送）；
- 最终目标是让模组在 1.20.1 尽可能完整稳定运行，而不仅是 `BUILD SUCCESSFUL`。

## 2. 当前状态一览

| 项目 | 值 |
| --- | --- |
| 工作分支 | `codex/forge-1.20.1`（仓库 `Ripples-of-the-Past-1.20.1`） |
| 基线提交 | 上游 1.16.5 `72862a5826ed45d1dc26e90b37853097acda35de` |
| 工具链 | Java 17、Forge 1.20.1-47.4.10、ForgeGradle 6.0.54、Gradle 8.8、官方（Mojang）映射、Mixin 0.8.5 |
| 编译错误数 | **324**（一次性 javac 报告数；轨迹 8,819 → 2,580 → 492 → 437 → 324） |
| 迁移提交数 | 125（相对 1.16.5 基线；每个系统一个里程碑，均未推送） |
| 当前阶段 | **长尾文件清理**（单文件 3–8 处），随后进入 Mixin/AT 复核 |
| 运行时测试 | **尚未开始**（构建尚未通过，不能宣称已测试） |

构建命令：

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
GRADLE_USER_HOME=/Users/administrator/CodexWorkspace/.carryon-gradle \
./gradlew --console=plain compileJava > .porting/build-NN.log 2>&1
grep -c 'error:' .porting/build-NN.log
python3 migration/tools/analyze_errors.py .porting/build-NN.log --symbols
```

## 3. 已完成的系统（按提交顺序）

| 系统 | 要点 |
| --- | --- |
| **构建与工具链** | ForgeGradle 6 / 官方映射 / 5 个运行目录；`mods.toml`、`pack.mcmeta`、`mixins.jojo.json`（JAVA_17）；依赖（playerAnimator、bendy-lib、JEI、Vampirism、ExpandAbility、MixinExtras、Mocha）全部解析通过 |
| **Access Transformer** | 全部改写为 1.20.1 的 **SRG 成员名**（含 `ModelPart`、`ModelPart$Cube/$Polygon`、`Screen`、`AbstractWidget`、`Gui`、`LivingEntityRenderer` 等），保留原私有成员访问，而不是改写为 getter |
| **自定义注册表** | `RegistryEntry` 统一注册表身份；`DeferredRegister.makeRegistry` |
| **Capability** | 全部 provider 迁移到 `CapabilityManager.get(new CapabilityToken<>(){})` + `ICapabilitySerializable<CompoundTag>`；`RegisterCapabilitiesEvent`；玩家克隆用 `reviveCaps/invalidateCaps`；`SaveFileUtilCapProvider.getSaveFileCap(Level/MinecraftServer/ServerPlayer)` 恢复（从主世界解析） |
| **网络** | `NetworkRegistry.newSimpleChannel` + `PROTOCOL_VERSION`；`NetworkUtil` 基于 `RegistryEntry` 读写注册表 id；Stand 生成数据单次 `readSpawnData` |
| **方块 / 物品 / 创造模式标签页** | 移除 `Material`/`ToolType`，改用 `liquid()/pushReaction()/requiresCorrectToolForDrops()` 与标签文件；`CreativeModeTab.builder()` + `addMainTabItems`（59 项，保持原顺序） |
| **模型与渲染基座** | 自定义 `ModelPart` 垫片（保留约 870 处声明式建模代码，经 1.20.1 builder 烘焙）；`GuiDraw` 垫片统一 GUI 绘制入口；`AbstractGui` 类型保留；所有渲染器改为 `EntityRendererProvider.Context`，注册并入 `EntityRendererRegisterRenderersEvent` |
| **SRG 反射名转换** | 新增 `migration/tools/convert_srg.py`：用 1.16.5 MCP+官方映射与 1.20.1 映射推导新 SRG 名（字段/方法分别联接、方法按描述符匹配），改对 68 处反射字符串 |
| **低层模型几何** | `ModelBox/TexturedQuad/PositionTextureVertex` → `ModelPart.Cube/Polygon/Vertex`；自定义立方体补齐 1.20.1 要求的可见面集合与显式纹理尺寸构造器；Blockbench 解析器同步 |
| **GUI 控件与提示框** | 按钮统一 `renderWidget(GuiGraphics,…)`；`Button.ITooltip` → `Tooltip` + `setTooltip`；普通按钮用 `Button.builder(...)`；屏幕改用 `GuiGraphics` |
| **HUD 覆盖层** | `RenderGameOverlayEvent` → `RegisterGuiOverlaysEvent` + `RenderGuiOverlayEvent`：失去视野/力竭暗角、动作 HUD（含文字层相位）、背负乌龟槽位、多行提示、GE 探测器数据；食物/氧气/生命/经验条/药水图标的取消与重绘全部保留 |
| **伤害系统** | 25 个数据驱动 `damage_type`（id 与 message id 沿用旧值）+ 原版旁路标签（`bypasses_armor`/`bypasses_resistance`/`bypasses_enchantments`/`bypasses_invulnerability`/`is_projectile`）；`ModDamageTypes` 从所在 level 解析类型；自定义伤害源不再继承已删除的 `EntityDamageSource` |
| **playerAnimator 弯曲** | 迁移到 1.20.1 无状态 `IBendHelper.bend(part, bendX, bendY)`；`KosmXBendyLibHelper` 负责按库自身方向（身体 DOWN、四肢 UP，取自库字节码）初始化并读回弯曲值 |
| **碰撞辅助** | `ReuseableStream` → `List`（`Shapes.collide` 接受 `Iterable`）；`Entity.getHorizontalDistanceSqr` → `Vec3.horizontalDistanceSqr()` |
| **数据序列化器** | 去掉 Forge 的 `DataSerializerEntry`，直接在 `ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS` 注册 |
| **worldgen** | 三个结构改为 1.20.1 `Structure` + `StructureType` + `GenerationStub`；碎片改用新 `TemplateStructurePiece` 构造器与 `StructurePieceType` 注册；间距/盐/群系/地形适配/配置特征全部落到 `data/jojo/worldgen`（群系限制用原版 `is_hill`/`is_jungle` 与自定义雪地非海洋标签）；配置开关仍在结构生成点判断 |

### 已核实的关键语义（避免臆测）

- 生产名规则：**官方类名 + SRG 成员名**（AT 与反射字符串都遵循）。
- `BendHelper.bend(part, a, b)`：反编译确认两参即 `bendX/bendY`（内部 `applyBend`），并确认库自身对 `body` 用 `DOWN`、四肢用 `UP` 初始化。
- `Cube` 构造器末两参为「纹理尺寸 × texScale」，与项目原有写法一致（1.20.1 的 `CubeDefinition.bake` 字节码确认）。
- 1.20.1 伤害类型的旁路属性由**标签**决定（不是 JSON 字段）。
- 雪地群系标签：1.20.1 取消了 precipitation 枚举，改为按群系温度数据推导（9 个雪地非海洋群系）。

## 4. 剩余工作

1. **长尾编译错误（约 492 处，单文件 3–8 处）**：以「整文件扫荡」方式推进，已清零的文件包括 `ClientSetup`、`GameplayEventHandler`、`MCUtil`、`CustomRenderType`、`LifeformsMetMobs`、`ClientConsciousnessEntity`、`HudLayoutEditingScreen`、`LifeformsList`、`ClientModSettingsScreen`（含控件）、`GELifeImbueGlint`、`ClientTickingSoundsHelper`、`NetworkUtil`、`WoodenCoffinBlock`、`GETransformationRenderer`、`ClientEventHandler`（部分）、`FirstPersonHamonAura`、`CrazyDiamondPreviousState` 等。
2. **Mixin 与 AT 复核**：目标选择器、SRG 字符串、注入点逐一核对；`VanillaKeyEntry` 依赖的原版屏幕 Mixin 字段（`selectedKey`）也在其中。
3. **Hamon Master 可弯曲衣物几何**：已按 bendy-lib 4.0.0 的 `MutableModelPart`/`BendableCuboid.Builder` 完成（原 `IBendHelper.create(...)` 在 1.20.1 无对应 API）。
4. **编译通过后的运行时测试（尚未开始）**：先在 `1.20.1-Forge` 实例中放入模组 jar 与前置（playerAnimator、bendy-lib），依次测试客户端、单人世界、专用服务器（注意客户端专用类隔离）。

## 4.1 已接受的有意差异（均已写入 README，非静默降级）

| 项目 | 1.16.5 行为 | 1.20.1 现状 | 影响 |
| --- | --- | --- | --- |
| Stand 发言消息 | 经 `ForgeHooks.onServerChatEvent`，其他模组可取消/改写 | 构造后按**系统消息**广播 | 其他模组无法再拦截该条消息；文本与发起者名不变 |
| `Screen#passEvents` | 任意屏幕可设置该字段以放行输入，模组读取它决定按键处理 | 1.20.1 无该字段、也无等价查询 | 模组自有 `WasdAllowingScreen` 行为已恢复；**第三方/原版**中靠该字段放行输入的屏幕，如今会拦截模组按键 |
| 拍立得离屏渲染 | 手工触发 `BasicEventHooks.onRenderTickStart/End` | 该 Forge 钩子类已删除，不再手工触发 | 离屏渲染期间其他模组的 render-tick 处理器不再被额外调用一次 |
| 柱人自爆伤害源 | `ON_FIRE` + `setExplosion()` | 自定义 `jojo:on_fire_explosion`（`message_id` 仍为 `onFire`），**同时**加入 `is_fire` 与 `is_explosion` | 完整恢复火焰与爆炸两类语义（**非差异**，记录以说明取舍） |
| 魔法伤害判定 | `DamageSource#isMagic()` | 模组自有 `jojo:magic` 标签（magic / indirect_magic / thorns） | 严格保持 1.16.5 集合，未纳入 1.20.1 新增的 `sonic_boom` |

## 5. 约束与安全边界（本次无人值守阶段）

- 允许：项目内文件读写/移动/删除（仅限移植必要）、Gradle 构建、公开依赖下载、公开文档只读访问、非破坏性 git 操作、**创建本地 commit**。
- 禁止：`git push`/force-push/rebase/破坏性 reset/amend/改写历史、删除仓库外文件、`sudo` 或系统级改动、访问密钥或私密信息、向任何外部服务上传/发布、全局安装软件、与移植无关的操作。
- 遇到未授权或不可逆的情形：跳过并继续其它安全工作，不猜测授权。
