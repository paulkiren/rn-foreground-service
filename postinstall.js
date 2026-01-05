const fs = require("fs");

const metadataTemplate = `
  <meta-data
    android:name="com.supersami.foregroundservice.notification_channel_name"
    android:value="Sticky Title"
  />
  <meta-data
    android:name="com.supersami.foregroundservice.notification_channel_description"
    android:value="Sticky Description."
  />
  <meta-data
    android:name="com.supersami.foregroundservice.notification_color"
    android:resource="@color/blue"
  />
  <service
      android:name="com.supersami.foregroundservice.ForegroundService"
      android:foregroundServiceType="dataSync|location|mediaPlayback"
      android:exported="false" />
  <service
      android:name="com.supersami.foregroundservice.ForegroundServiceTask"
      android:exported="false" />
`;

const androidManifestPath = `${process.cwd()}/android/app/src/main/AndroidManifest.xml`;

console.log('\n📱 @kirenpaul/rn-foreground-service - Post-install setup');
console.log('================================================\n');

fs.readFile(androidManifestPath, "utf8", function (err, data) {
  if (err) {
    console.log('⚠️  Could not read AndroidManifest.xml - you may need to configure manually.');
    console.log('   Path:', androidManifestPath);
    return console.log('   Error:', err.message);
  }

  // Check if services are already configured
  const hasForegroundService = data.includes('com.supersami.foregroundservice.ForegroundService');
  const hasTaskService = data.includes('com.supersami.foregroundservice.ForegroundServiceTask');

  if (hasForegroundService && hasTaskService) {
    console.log('✅ Services already configured in AndroidManifest.xml');

    // Check if using old format without foregroundServiceType
    if (data.includes('<service android:name="com.supersami.foregroundservice.ForegroundService"></service>')) {
      console.log('\n⚠️  WARNING: Your manifest has the OLD service declaration format.');
      console.log('   Android 14+ requires foregroundServiceType attribute.');
      console.log('   Please update your AndroidManifest.xml with the new format.');
      console.log('   See: https://github.com/paulkiren/rn-foreground-service#android-14-requirements\n');
    } else {
      console.log('✅ Service types configured correctly for Android 14+\n');
    }
    return;
  }

  // Inject services
  const reg = /<application[^>]*>/;
  const match = reg.exec(data);

  if (!match) {
    console.log('⚠️  Could not find <application> tag in AndroidManifest.xml');
    console.log('   Please add services manually. See README.md for instructions.\n');
    return;
  }

  const content = match[0];
  const result = data.replace(reg, `${content}${metadataTemplate}`);

  fs.writeFile(androidManifestPath, result, "utf8", function (err) {
    if (err) {
      console.log('⚠️  Failed to write AndroidManifest.xml');
      return console.log('   Error:', err.message);
    }

    console.log('✅ Successfully configured AndroidManifest.xml');
    console.log('   - Added ForegroundService with types: dataSync|location|mediaPlayback');
    console.log('   - Added ForegroundServiceTask');
    console.log('   - Added notification metadata\n');

    console.log('⚠️  IMPORTANT: Android 14+ Requirements');
    console.log('   You must add these permissions to your AndroidManifest.xml:\n');
    console.log('   <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />');
    console.log('   <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />');
    console.log('   <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />\n');
    console.log('   For location tracking, also add:');
    console.log('   <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />\n');
    console.log('   See README.md for complete setup instructions.');
    console.log('================================================\n');
  });
});

const colorTemplate = `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="blue" type="color">#00C4D1</item>
    <integer-array name="androidcolors">
        <item>@color/blue</item>
    </integer-array>
</resources>
`;

const colorFilePath = `${process.cwd()}/android/app/src/main/res/values/colors.xml`;

// Check if colors.xml already exists
fs.readFile(colorFilePath, "utf8", function (err, existingData) {
  if (!err && existingData) {
    // File exists, check if our color is already there
    if (existingData.includes('name="blue"')) {
      console.log('✅ colors.xml already has required colors configured\n');
      return;
    }

    // File exists but doesn't have our colors - warn user
    console.log('⚠️  colors.xml exists but may need manual color addition');
    console.log('   Add this color if not present: <item name="blue" type="color">#00C4D1</item>\n');
    return;
  }

  // Create new colors.xml
  const valuesDir = `${process.cwd()}/android/app/src/main/res/values`;

  // Ensure values directory exists
  if (!fs.existsSync(valuesDir)) {
    try {
      fs.mkdirSync(valuesDir, { recursive: true });
    } catch (mkdirErr) {
      console.log('⚠️  Could not create values directory:', mkdirErr.message);
      return;
    }
  }

  fs.writeFile(colorFilePath, colorTemplate, "utf8", function (err) {
    if (err) {
      console.log('⚠️  Could not create colors.xml');
      return console.log('   Error:', err.message);
    }

    console.log('✅ Successfully created colors.xml\n');
  });
});
