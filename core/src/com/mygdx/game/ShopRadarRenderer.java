package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class ShopRadarRenderer extends ApplicationAdapter {
    private static final int INITIAL_CAMERA_VIEWPORT = 10;
    private SpriteBatch batch;
    private Sprite radarSprite;
    private Sprite dotSprite;
    private OrthographicCamera camera;
    private List<ShopItemDrawable> shopItemDrawables = new ArrayList<>();
    private double clientLatitude;
    private double clientLongitude;

    public ShopRadarRenderer(double clientLatitude, double clientLongitude) {
        this.clientLatitude = clientLatitude;
        this.clientLongitude = clientLongitude;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        radarSprite = new Sprite(new Texture("radar.jpg"));
        dotSprite = new Sprite(new Texture("dot.jpg"));
        dotSprite.setSize(0.5f, 0.5f);
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera(INITIAL_CAMERA_VIEWPORT, INITIAL_CAMERA_VIEWPORT * (h / w));
		double[] latLngMeters = relativeLatLonToMeters(50,0, clientLatitude, clientLongitude);
        camera.position.set((float) latLngMeters[1], (float) latLngMeters[0], 0);
        camera.update();
    }

    @Override
    public void render() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        ScreenUtils.clear(0, 0, 0, 1, true);
        batch.begin();
        renderRadar();
        renderShopItemDrawables();
        batch.end();
    }

    private void renderShopItemDrawables() {
        shopItemDrawables.forEach(shopItemDrawable -> {
			double[] latLngMeters = relativeLatLonToMeters(50,0, shopItemDrawable.getLatitude(), shopItemDrawable.getLongitude());
            dotSprite.setPosition((float) latLngMeters[1] - dotSprite.getWidth() / 2, (float) latLngMeters[0] - dotSprite.getHeight() / 2);
            dotSprite.draw(batch);
        });
    }
    private void renderRadar() {
        radarSprite.setSize(camera.viewportWidth, camera.viewportHeight);//TODO!
        radarSprite.setPosition(camera.position.x - camera.viewportWidth / 2, camera.position.y - camera.viewportHeight / 2);
        radarSprite.draw(batch);
    }

    @Override
    public void dispose() {
        batch.dispose();
        dotSprite.getTexture().dispose();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = INITIAL_CAMERA_VIEWPORT;
        camera.viewportHeight = (float) (INITIAL_CAMERA_VIEWPORT * height) / width;
        camera.update();
    }

    public void passShopItemDrawables(List<ShopItemDrawable> shopItemDrawables) {
        this.shopItemDrawables = shopItemDrawables;
    }

    public void passClientPosition(double latitude, double longitude) {
        this.clientLatitude = latitude;
        this.clientLongitude = longitude;
		double[] latLngMeters = relativeLatLonToMeters(50,0, clientLatitude, clientLongitude);
		camera.position.set((float) latLngMeters[1], (float) latLngMeters[0], 0);
    }

	private double[] relativeLatLonToMeters(double lat, double lon, double relLatChange, double relLonChange) {
		// Conversion factors
		double latFactor = 111000; // meters per degree of latitude
		double lonFactor = 111000 * Math.cos(Math.toRadians(lat)); // meters per degree of longitude

		// Calculate absolute changes in meters
		double latMeters = relLatChange * latFactor;
		double lonMeters = relLonChange * lonFactor;

		return new double[]{latMeters, lonMeters};
	}
}
