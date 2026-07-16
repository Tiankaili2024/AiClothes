import subprocess
# Restore from git
result = subprocess.run(['git', 'show', 'HEAD:frontend/src/views/outfit/OutfitChatView.vue'], capture_output=True)
with open('frontend/src/views/outfit/OutfitChatView.vue', 'wb') as f:
    f.write(result.stdout)
print("Restored from git")

content = open('frontend/src/views/outfit/OutfitChatView.vue', 'r', encoding='utf-8').read()

# Change 1: image_url src
old1 = '                  :src="' + "'" + 'https://placehold.co/400x500/e8f0f6/8aaec8?text=' + "' + encodeURIComponent(currentPlan.scheme_name || '穿搭效果')" + '"'
new1 = '                  :src="currentPlan.image_url || ' + "'" + 'https://placehold.co/400x500/e8f0f6/8aaec8?text=' + "' + encodeURIComponent(currentPlan.scheme_name || '穿搭效果')" + '"'

if old1 in content:
    content = content.replace(old1, new1)
    print("Change 1 applied")
else:
    print("Change 1 FAILED")

# Change 2: inject image_url before schemes
old2 = '      schemes.value = plans'
new2 = '      if (record.imageUrl && plans.length > 0) {\n        plans[0].image_url = record.imageUrl\n      }\n      schemes.value = plans'
if old2 in content:
    content = content.replace(old2, new2)
    print("Change 2 applied")
else:
    print("Change 2 FAILED")

# Change 3: retry logic in sendMessage
start_marker = '  try {\n    const res = await generateOutfit({\n      userInput: inputText,\n      city: cityInput.value || DEFAULT_CITY\n    })'
idx_start = content.find(start_marker)
idx_catch = content.find('  } catch (e) {', idx_start)
idx_end = content.find('  }\n  generating.value = false', idx_catch) + 2
old3 = content[idx_start:idx_end]

new3 = '''  for (let attempt = 1; attempt <= 2; attempt++) {
    try {
      const controller = new AbortController()
      setTimeout(() => controller.abort(), 30000)

      const res = await generateOutfit({
        userInput: inputText,
        city: cityInput.value || DEFAULT_CITY
      })

      if (res.code === 200 && res.data) {
        const record = res.data
        let plans = []
        try {
          const parsed = JSON.parse(record.parsedParams || '[]')
          plans = Array.isArray(parsed) ? parsed : (parsed ? [parsed] : [])
        } catch (e) { plans = [] }
        if (!plans.length) {
          plans = [{ scheme_name: '\u63a8\u8350\u642d\u914d', style: '\u901a\u52e4', color: '\u6d45\u8272\u7cfb', top: '\u2014', bottom: '\u2014', shoes: '\u2014', accessory: '\u2014' }]
        }
        if (record.imageUrl && plans.length > 0) plans[0].image_url = record.imageUrl
        schemes.value = plans
        activeScheme.value = 0

        const D = String.fromCharCode(96)
        const planNames = plans.map((p, i) => D + '\u65b9\u6848' + (i + 1) + ': ' + (p.scheme_name || p.style || '') + D).join('\u3001')
        messages.value.push({
          role: 'ai',
          content: D + '\u5df2\u4e3a\u60a8\u751f\u6210 ' + plans.length + ' \u5957\u7a7f\u642d\u65b9\u6848 \\n' + planNames + '\\n\u70b9\u51fb\u53f3\u4fa7\u67e5\u770b\u8be6\u7ec6\u642d\u914d\u89e3\u6790' + D,
          imageUrl: record.imageUrl || '',
          recordId: record.id,
          params: plans,
        })
        await checkFavStatus(record.id)
        await loadTodayCount()
        return
      }
    } catch (e) {
      if (attempt < 2) await new Promise(r => setTimeout(r, 2000))
      else messages.value.push({ role: 'ai', content: '\u751f\u6210\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5' })
    }
  }'''

if old3:
    content = content.replace(old3, new3)
    print("Change 3 applied")
else:
    print("Change 3 FAILED")

with open('frontend/src/views/outfit/OutfitChatView.vue', 'w', encoding='utf-8') as f:
    f.write(content)
print("File saved")
