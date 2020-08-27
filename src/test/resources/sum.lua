function sum()
    local s = 0
    for i = 1, 100 do
        if i % 2 == 0 then
            s = s + math.sqrt(i)
        end
    end
    return s
end
print(sum())

