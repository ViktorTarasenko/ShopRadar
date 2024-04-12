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
    private static final int WORLD_SIZE = 5000;
    private static final int INITIAL_CAMERA_VIEWPORT = 10;
    private SpriteBatch batch;
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
        dotSprite = new Sprite(new Texture("badlogic.jpg"));
        dotSprite.setSize(1, 1);
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
        Gdx.gl.glClearColor(0, 1, 0, 1);
        batch.begin();
        renderShopItemDrawables();
        batch.end();
    }

    private void renderShopItemDrawables() {
        shopItemDrawables.forEach(shopItemDrawable -> {
			double[] latLngMeters = relativeLatLonToMeters(50,0, shopItemDrawable.getLatitude(), shopItemDrawable.getLongitude());
            dotSprite.setPosition((float) latLngMeters[1], (float) latLngMeters[0]);
            dotSprite.draw(batch);
        });
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
