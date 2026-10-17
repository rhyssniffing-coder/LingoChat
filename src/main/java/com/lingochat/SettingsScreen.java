package com.lingochat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SettingsScreen extends Screen {
	private ButtonWidget incomingLanguageButton;
	private ButtonWidget outgoingLanguageButton;
	private ButtonWidget autoOutgoingButton;
	private ButtonWidget autoIncomingButton;

	public SettingsScreen() {
		super(Text.literal("LingoChat Settings"));
	}

	@Override
	protected void init() {
		Config config = Config.get();
		int x = this.width / 2 - 120;
		int y = this.height / 2 - 50;

		this.incomingLanguageButton = ButtonWidget.builder(incomingLabel(), button -> {
			Config.get().incomingLanguage = Langs.next(Config.get().incomingLanguage, MinecraftClient.getInstance().isShiftPressed());
			Config.save();
			button.setMessage(incomingLabel());
		}).dimensions(x, y, 240, 20).build();
		this.addDrawableChild(this.incomingLanguageButton);

		this.autoIncomingButton = ButtonWidget.builder(autoIncomingLabel(), button -> {
			Config.get().autoTranslateIncoming = !Config.get().autoTranslateIncoming;
			Config.save();
			button.setMessage(autoIncomingLabel());
		}).dimensions(x, y + 24, 240, 20).build();
		this.addDrawableChild(this.autoIncomingButton);

		this.autoOutgoingButton = ButtonWidget.builder(autoOutgoingLabel(), button -> {
			Config.get().autoTranslateOutgoing = !Config.get().autoTranslateOutgoing;
			Config.save();
			button.setMessage(autoOutgoingLabel());
			this.outgoingLanguageButton.active = Config.get().autoTranslateOutgoing;
		}).dimensions(x, y + 48, 240, 20).build();
		this.addDrawableChild(this.autoOutgoingButton);

		this.outgoingLanguageButton = ButtonWidget.builder(outgoingLabel(), button -> {
			Config.get().outgoingLanguage = Langs.next(Config.get().outgoingLanguage, MinecraftClient.getInstance().isShiftPressed());
			Config.save();
			button.setMessage(outgoingLabel());
		}).dimensions(x, y + 72, 240, 20).build();
		this.outgoingLanguageButton.active = config.autoTranslateOutgoing;
		this.addDrawableChild(this.outgoingLanguageButton);

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> this.close())
			.dimensions(this.width / 2 - 40, y + 104, 80, 20).build());
	}

	private Text incomingLabel() {
		return Text.literal("Translate chat to: " + Langs.nameOf(Config.get().incomingLanguage));
	}

	private Text outgoingLabel() {
		return Text.literal("Send my messages in: " + Langs.nameOf(Config.get().outgoingLanguage));
	}

	private Text autoOutgoingLabel() {
		return Text.literal("Auto-translate my messages: " + (Config.get().autoTranslateOutgoing ? "ON" : "OFF"));
	}

	private Text autoIncomingLabel() {
		return Text.literal("Auto-translate incoming chat: " + (Config.get().autoTranslateIncoming ? "ON" : "OFF"));
	}

	@Override
	public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 70, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, "Open chat and click a message to translate it", this.width / 2, this.height / 2 - 30, 0xA0A0A0);
		context.drawCenteredTextWithShadow(this.textRenderer, "Shift-click a language button to go backwards", this.width / 2, this.height / 2 + 104, 0x808080);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
