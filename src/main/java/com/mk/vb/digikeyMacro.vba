Sub DigiKey()
            PartLink = "http://www.digikey.com/product-search/en?vendor=0&keywords=" + PartNum
            IE.navigate PartLink
            Do
                DoEvents
            Loop Until IE.readyState = 4

            temp_bool = False
            temp_bool2 = True
            Set objCollection = IE.document.getElementsByTagName("html")
            Pageid = objCollection(0).className

            If Pageid = "rd-search-parts-page" Then
                GoTo NotFound
            End If
        '     Set objCollection = IE.document.getElementsByTagName("body")
        '     Debug.Print objCollection(0).innertext
        '    Our site is currently being updated. Please retry your request in a few minutes.

            If Pageid = "rd-product-details-page" Then
                Set objCollection = IE.document.getElementsByTagName("link")
                PartLink = objCollection(3).href
                GoTo GotLink
            End If

            If Pageid = "rd-product-category-page" Then
                Set objCollection = IE.document.getElementsByTagName("table")
                For k = 0 To objCollection.Length
                    If objCollection(k).ID = "productTable" Then
                        Exit For
                    End If
                Next
            End If

            Set objCollection1 = objCollection(k).getElementsByTagName("tbody")
            Set objCollection2 = objCollection1(0).getElementsByTagName("td")

            For Each objElement2 In objCollection2
                If InStr(objElement2.innerHTML, "/product-detail/en/") <> 0 Then
                    temp_string = objElement2.innerHTML
                    Parts = Split(temp_string, Chr$(34))
                    PartLink = "http://www.digikey.com" + Parts(1)
                    Exit For
                End If
            Next

GotLink:
            IE.navigate PartLink
            Do
                DoEvents
            Loop Until IE.readyState = 4

            Call DigiExtraction
            Call DigiFillEXcel
NotFound:
End Sub