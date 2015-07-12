Sub MouserExtraction()
    IE.navigate PartLink
    Do
        DoEvents
    Loop Until IE.readyState = 4
    
    Set theTables = IE.document.all.tags("table")
    nTables = theTables.Length
    pbrk = 0
    t = 0
    For Each Table In theTables
        If Table.className = "productdetail-box1" Then
            Exit For
        End If
        t = t + 1
    Next
    
    For r = 0 To Table.Rows.Length - 1
        For c = 0 To Table.Rows(r).Cells.Length - 1
                CompDetail = Table.Rows(r).Cells(c).innertext
                Parts = Split(CompDetail, Chr(13) + Chr(10))
                
                For p = 0 To UBound(Parts)
                    CompDetail = Trim(Parts(p))
                    If InStr(1, CompDetail, "Mouser Part #:", vbTextCompare) And MouserPart = False Then
                        MouserPart = True
                        MouserPartNum = Trim(Parts(p + 1))
                        If MouserPartNum = "Not Assigned" Then
                            GoTo NotAssigned
                        End If
                    End If
                    If InStr(1, CompDetail, "Manufacturer Part #:", vbTextCompare) And MfrPart = False Then
                        MfrPart = True
                        MfrPartNum = Trim(Parts(p + 2))
                    End If
                    If InStr(1, CompDetail, "Manufacturer:", vbTextCompare) And Mfr = False Then
                        Mfr = True
                        MfrName = Trim(Parts(p + 1))
                    End If
                    If InStr(1, CompDetail, "Description:", vbTextCompare) And Description = False Then
                        Parts = Split(CompDetail, ":")
                        Description = True
                        PartDescription = Trim(Parts(1))
                        GoTo SkipDetail1
                    End If
                Next p
            Next c
        Next r

SkipDetail1:

    t = 0
    For Each Table In theTables
        If Table.className = "productdetail-box2" Then
            Exit For
        End If
        t = t + 1
    Next
    pbrk = 0
    For r = 0 To Table.Rows.Length - 1
        For c = 0 To Table.Rows(r).Cells.Length - 1
            CompDetail = Table.Rows(r).Cells(c).innertext
            Parts = Split(CompDetail, Chr(13) + Chr(10))

            For p = 0 To UBound(Parts)
                CompDetail = Parts(p)
                If InStr(1, CompDetail, "Stock:", vbTextCompare) And Stock = False Then
                    Stock = True
                    subParts = Split(Parts(p + 1), "Can ")
                    MouserStock = Trim(subParts(0))
                    NumStr = MouserStock
                    Call StrToNum
                    MouserStock = NumStr
                    GoTo StockDone
                End If
            Next p
        Next c
    Next r
    
StockDone:
    Open "Z:\PNC Assembly\RFQ Script\PriceBreak.txt" For Output As #1
    PriceLocEnd = 1
    CompDetail = Table.Rows(r).Cells(c).innertext
    Print #1, CompDetail
    Close #1
    Open "Z:\PNC Assembly\RFQ Script\PriceBreak.txt" For Input As #1
    Do Until EOF(1)
        Line Input #1, Detail
        Debug.Print Detail
        If InStr(1, Detail, ": $", vbTextCompare) Then
            
            subParts = Split(Detail, ": $")
            NumStr = subParts(0)
            Call StrToNum
            PriceBreak(pbrk, 0) = NumStr
            PriceBreak(pbrk, 1) = subParts(1)
            pbrk = pbrk + 1
        End If
    Loop
    
    Close #1
    pbrk = pbrk - 1
    t = 0
    
    For Each Table In theTables
        If Table.className = "specs" Then
            Exit For
        End If
        t = t + 1
    Next
    
    For r = 0 To Table.Rows.Length - 1
        For c = 0 To Table.Rows(r).Cells.Length - 1
            CompDetail = Table.Rows(r).Cells(c).innertext
            Parts = Split(CompDetail, Chr(13) + Chr(10))
            For p = 0 To UBound(Parts)
                If InStr(1, CompDetail, "Product Category:", vbTextCompare) Then
                    subParts = Split(Parts(p), ":")
                    Category = Trim(subParts(1))
                End If
                If InStr(1, CompDetail, "Package / Case:", vbTextCompare) Then
                    subParts = Split(Parts(p), ":")
                    Package_Case = Trim(subParts(1))
                End If
                If InStr(1, CompDetail, "Packaging:", vbTextCompare) Then
                    subParts = Split(Parts(p), ":")
                    Packaging = Trim(subParts(1))
                End If
                If InStr(1, CompDetail, "Type:", vbTextCompare) Then
                    subParts = Split(Parts(p), ":")
                    Family = Trim(subParts(1))
                End If
                
            Next p
        Next c
    Next r
NotAssigned:
End Sub


