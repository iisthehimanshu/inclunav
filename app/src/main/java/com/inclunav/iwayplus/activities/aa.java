//new AsyncTask<Void, Void, BuildingDataEntity>() {
//@Override
//protected BuildingDataEntity doInBackground(Void... voids) {
//        // Retrieve data from the Room Database here
//        return appDatabase.buildingDataDao().getBuildingDataByBuildingName(initialBuildingName);
//        }
//
//@Override
//protected void onPostExecute(BuildingDataEntity cachedData) {
//        if (cachedData != null) {
//        // Data is available locally, use it
//        try {
//        JSONArray response = new JSONArray(cachedData.getResponseData());
//        processBuildingData(response);
//        } catch (JSONException e) {
//        e.printStackTrace();
//        }
//        }
//        }
//        }.execute();