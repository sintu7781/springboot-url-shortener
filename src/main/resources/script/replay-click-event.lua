local replayKey = KEYS[1]
local streamKey = KEYS[2]

local event = ARGV[1]
local maxLength = ARGV[2]
local replayedAt = ARGV[3]

local existing = redis.call(
    'GET',
    replayKey
)

if existing then
    return 'ALREADY_REPLAYED:' .. existing
end

local recordId = redis.call(
    'XADD',
    streamKey,
    'MAXLEN',
    '~',
    maxLength,
    '*',
    'event',
    event
)

local auditData =
    '{"newStreamId":"' ..
    recordId ..
    '","replayedAt":"' ..
    replayedAt ..
    '"}'

redis.call(
    'SET',
    replayKey,
    auditData
)

return 'REPLAYED:' .. recordId
